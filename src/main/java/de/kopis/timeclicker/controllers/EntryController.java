package de.kopis.timeclicker.controllers;

import de.kopis.timeclicker.Application;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.UserSettings;
import de.kopis.timeclicker.model.wrappers.EntryCount;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/entries")
public class EntryController {
    private TimeclickerAPI api = new TimeclickerAPI();

    private UserService userService = UserServiceFactory.getUserService();

    private final Logger LOG = LoggerFactory.getLogger(EntryController.class);

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "31") int limit,
                       @RequestParam(defaultValue = "0") int page) throws NotAuthenticatedException {
        final User user = userService.getCurrentUser();
        final List<TimeEntry> entries = api.list(limit, page, user);
        model.addAttribute("entries", entries);

        // build pagination
        final EntryCount maxEntries = api.countAvailableEntries(user);
        final int lastPage = maxEntries.count / limit;
        model.addAttribute("limit", limit);
        model.addAttribute("lastPage", lastPage);
        model.addAttribute("previousPage", (page > 0) ? (page - 1) : page);
        model.addAttribute("nextPage", (page < lastPage) ? (page + 1) : page);

        return "entries/list";
    }

    @PostMapping
    public String create(@ModelAttribute TimeEntry input) throws NotAuthenticatedException {
        final User user = userService.getCurrentUser();

        // duration is optional
        Duration breakDuration = input.getBreakDuration() != null ? input.getBreakDuration() : Duration.of(0, ChronoUnit.SECONDS);
        // stop is optional
        Date stop = null;
        Instant inputStop = input.getStop();
        if(inputStop != null) {
            stop = Date.from(inputStop);
        }

        api.update(input.getKey(),
                Date.from(input.getStart()), stop, breakDuration.toMillis(),
                input.getDescription(), input.getTags(), input.getProject(),
                user);

        return "redirect:/entries";
    }

    @GetMapping("/{key}")
    public String get(Model model, @PathVariable String key) throws NotAuthenticatedException {
        final User user = userService.getCurrentUser();
        final TimeEntry entry = api.show(key, user);
        if (entry != null) {
            model.addAttribute("entry", entry);
        }

        return "entries/add";
    }

    @DeleteMapping("/{key}")
    public String delete(@PathVariable String key) throws NotAuthenticatedException {
        final User user = userService.getCurrentUser();
        api.delete(key, user);

        return "redirect:/entries";
    }

    @GetMapping("/add")
    public String create(Model model) throws NotAuthenticatedException {
        model.addAttribute("entry", new TimeEntry());

        final User user = userService.getCurrentUser();
        UserSettings userSettings = api.getUserSettings(null, user);
        int offset = userSettings.getTimezone().getOffset(Instant.now().toEpochMilli());
        model.addAttribute("timezoneOffset", offset);

        return "entries/add";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(Application.DATE_FORMAT, true, 19));
    }
}
