package de.kopis.timeclicker.controllers;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.EntryNotOwnedByUserException;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.exceptions.RatingNotOwnedByUserException;
import de.kopis.timeclicker.model.EmotionRating;
import de.kopis.timeclicker.model.UserSettings;
import de.kopis.timeclicker.model.wrappers.EntryCount;

import java.time.Instant;
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
@RequestMapping("/ratings")
public class RatingController {
  private TimeclickerAPI api = new TimeclickerAPI();

  private UserService userService = UserServiceFactory.getUserService();

  private final Logger LOGGER = LoggerFactory.getLogger(EntryController.class);

  @GetMapping
  public String list(Model model,
                     @RequestParam(defaultValue = "31") int limit,
                     @RequestParam(defaultValue = "0") int page) throws NotAuthenticatedException {
    final User user = userService.getCurrentUser();
    final List<EmotionRating> entries = api.listRatings(limit, page, user);
    model.addAttribute("entries", entries);

    // build pagination
    final EntryCount maxEntries = api.countAvailableEntries(user);
    final int lastPage = maxEntries.count / limit;
    model.addAttribute("limit", limit);
    model.addAttribute("lastPage", lastPage);
    model.addAttribute("previousPage", (page > 0) ? (page - 1) : page);
    model.addAttribute("nextPage", (page < lastPage) ? (page + 1) : page);

    return "ratings/list";
  }

  @PostMapping
  public String create(@ModelAttribute EmotionRating input) throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final User user = userService.getCurrentUser();

    LOGGER.info("Creating new rating: {}", input);

    api.rate(input, user);

    return "redirect:/ratings";
  }

  @GetMapping("/add")
  public String create(Model model) throws NotAuthenticatedException, EntryNotOwnedByUserException {
    model.addAttribute("rating", new EmotionRating());

    final User user = userService.getCurrentUser();
    UserSettings userSettings = api.getUserSettings(null, user);
    int offset = userSettings.getTimezone().getOffset(Instant.now().toEpochMilli());
    model.addAttribute("timezoneOffset", offset);

    return "ratings/add";
  }

  @GetMapping("/{key}")
  public String get(Model model, @PathVariable String key) throws NotAuthenticatedException, RatingNotOwnedByUserException {
    final User user = userService.getCurrentUser();
    final EmotionRating rating = api.showRating(key, user);
    if (rating != null) {
      model.addAttribute("rating", rating);
    }

    return "ratings/add";
  }

  @DeleteMapping("/{key}")
  public String delete(@PathVariable String key) throws NotAuthenticatedException, RatingNotOwnedByUserException {
    final User user = userService.getCurrentUser();
    api.deleteRating(key, user);

    return "redirect:/ratings";
  }
}
