package de.kopis.timeclicker.controllers;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.EntryNotOwnedByUserException;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.UserSettings;
import de.kopis.timeclicker.model.wrappers.EntryCount;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/entries")
public class EntryController {
  private TimeclickerAPI api = new TimeclickerAPI();

  private UserService userService = UserServiceFactory.getUserService();

  private final Logger LOGGER = LoggerFactory.getLogger(EntryController.class);

  @GetMapping
  public String list(Model model,
                     @RequestParam(required = false) List<String> tags,
                     @RequestParam(defaultValue = "31") int limit,
                     @RequestParam(defaultValue = "0") int page) throws NotAuthenticatedException {
    final User user = userService.getCurrentUser();
    if(tags == null) {
      tags = Collections.emptyList();
    }
    final List<TimeEntry> entries = api.list(String.join(",", tags), limit, page, user);
    model.addAttribute("entries", entries);

    // build pagination
    final EntryCount maxEntries = api.countAvailableEntries(user);
    final int lastPage = maxEntries.count / limit;
    model.addAttribute("tags", tags);
    model.addAttribute("limit", limit);
    model.addAttribute("lastPage", lastPage);
    model.addAttribute("previousPage", (page > 0) ? (page - 1) : page);
    model.addAttribute("nextPage", (page < lastPage) ? (page + 1) : page);

    return "entries/list";
  }

  @PostMapping
  public String create(@ModelAttribute TimeEntry input) throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final User user = userService.getCurrentUser();

    // duration is optional
    Duration breakDuration = input.getBreakDuration() != null ? input.getBreakDuration() : Duration.of(0, ChronoUnit.SECONDS);
    // stop is optional
    Date stop = null;
    Instant inputStop = input.getStop();
    if (inputStop != null) {
      stop = Date.from(inputStop);
    }

    api.update(input.getKey(),
        Date.from(input.getStart()), stop, breakDuration.toMillis(),
        input.getDescription(), input.getTags(), input.getProject(),
        user);

    return "redirect:/entries";
  }

  @GetMapping("/{key}")
  public String get(Model model, @PathVariable String key) throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final User user = userService.getCurrentUser();
    final TimeEntry entry = api.show(key, user);
    if (entry != null) {
      model.addAttribute("entry", entry);
    }

    return "entries/add";
  }

  @DeleteMapping("/{key}")
  public String delete(@PathVariable String key) throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final User user = userService.getCurrentUser();
    api.delete(key, user);

    return "redirect:/entries";
  }

  @GetMapping("/add")
  public String create(Model model) throws NotAuthenticatedException, EntryNotOwnedByUserException {
    model.addAttribute("entry", new TimeEntry());

    final User user = userService.getCurrentUser();
    UserSettings userSettings = api.getUserSettings(null, user);
    int offset = userSettings.getTimezone().getOffset(Instant.now().toEpochMilli());
    model.addAttribute("timezoneOffset", offset);

    return "entries/add";
  }
}
