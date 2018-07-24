package de.kopis.timeclicker.controllers;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@Controller
@RequestMapping("/")
public class HomeController {
  private TimeclickerAPI api = new TimeclickerAPI();

  private UserService userService = UserServiceFactory.getUserService();

  @GetMapping()
  public String home(Model model) throws NotAuthenticatedException {
    final User user = userService.getCurrentUser();
    final TimeEntry latest = api.latest(user);
    if (latest != null) {
      model.addAttribute("entry", latest);
      final TimeSum sum = new TimeSum(latest);
      model.addAttribute("sum", sum);
    }

    final TimeSum dailySum = api.getDailySum(user);
    model.addAttribute("dailySum", dailySum);
    final TimeSum weeklySum = api.getWeeklySum(user);
    model.addAttribute("weeklySum", weeklySum);
    final TimeSum monthlySum = api.getMonthlySum(user);
    model.addAttribute("monthlySum", monthlySum);

    return "index";
  }
}
