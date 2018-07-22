package de.kopis.timeclicker.controllers;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.Application;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.wrappers.EntryCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

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

        api.update(input.getKey(),
                Date.from(input.getStart()), Date.from(input.getStop()), breakDuration.toMillis(),
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
    public String create(Model model) {
        model.addAttribute("entry", new TimeEntry());

        return "entries/add";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(Application.DATE_FORMAT, true, 19));
    }
}
