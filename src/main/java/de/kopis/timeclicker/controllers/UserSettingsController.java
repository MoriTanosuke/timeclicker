package de.kopis.timeclicker.controllers;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.UserSettings;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.TimeZone;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@Controller
@RequestMapping("/settings")
public class UserSettingsController {
  private TimeclickerAPI api = new TimeclickerAPI();
  private UserService userService = UserServiceFactory.getUserService();

  @GetMapping
  public String showUserSettings(Model model) throws NotAuthenticatedException {
    final User user = userService.getCurrentUser();
    final UserSettings userSettings = api.getUserSettings(null, user);
    model.addAttribute("settings", userSettings);

    // add all timezones
    String[] availableIDs = TimeZone.getAvailableIDs();
    Arrays.sort(availableIDs);
    model.addAttribute("timezones", availableIDs);

    // add all locales
    Locale[] locales = Locale.getAvailableLocales();
    Arrays.sort(locales, Comparator.comparing(Locale::getDisplayName));
    model.addAttribute("locales", locales);

    return "settings/add";
  }

  @PostMapping
  public String saveUserSettings(Model model, @ModelAttribute UserSettings settings) throws NotAuthenticatedException, EntityNotFoundException {
    final User user = userService.getCurrentUser();

    // check key
    if (settings.getKey() != null && settings.getKey().isEmpty()) {
      settings.setKey(null);
    }

    api.setUserSettings(settings, user);

    return "redirect:/settings";
  }

}
