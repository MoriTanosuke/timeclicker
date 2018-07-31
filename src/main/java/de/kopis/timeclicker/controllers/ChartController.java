package de.kopis.timeclicker.controllers;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSumWithDate;
import de.kopis.timeclicker.model.UserSettings;
import de.kopis.timeclicker.utils.TimeSumUtility;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@Controller
@RequestMapping("/charts")
public class ChartController {
  private final TimeclickerAPI api = new TimeclickerAPI();
  private final UserService userService = UserServiceFactory.getUserService();
  private static final TimeSumUtility TIME_SUM_UTILITY = new TimeSumUtility();

  @GetMapping("/weekly")
  public String getWeeklyChart(Model model,
                               @RequestParam(defaultValue = "31") int limit,
                               @RequestParam(defaultValue = "0") int page) throws NotAuthenticatedException {
    final User user = userService.getCurrentUser();

    final List<TimeEntry> allEntries = api.list(limit, page, user);
    final List<TimeSumWithDate> sortedPerWeek = TIME_SUM_UTILITY.calculateWeeklyTimeSum(allEntries);
    Collections.sort(sortedPerWeek, Comparator.comparing(TimeSumWithDate::getDate));
    model.addAttribute("weeklySums", sortedPerWeek);

    final UserSettings settings = api.getUserSettings(null, user);
    final Map<Date, Duration> remaining = new TreeMap<>();
    sortedPerWeek.forEach(sum -> {
      Duration weeklyDuration = settings.getWorkingDurationPerDay().multipliedBy(5).minus(sum.getDuration());
      remaining.put(sum.getDate(), weeklyDuration);
    });
    model.addAttribute("remaining", remaining);

    return "charts/weekly";
  }

  @GetMapping("/daily")
  public String getDailyChart(Model model,
                              @RequestParam(defaultValue = "31") int limit,
                              @RequestParam(defaultValue = "0") int page) throws NotAuthenticatedException {
    final User user = userService.getCurrentUser();

    final List<TimeEntry> allEntries = api.list(limit, page, user);
    final List<TimeSumWithDate> sortedPerDay = new TimeSumUtility().calculateDailyTimeSum(allEntries);
    Collections.sort(sortedPerDay, Comparator.comparing(TimeSumWithDate::getDate));
    model.addAttribute("dailySums", sortedPerDay);

    final UserSettings settings = api.getUserSettings(null, user);
    final Map<Date, Duration> remaining = new TreeMap<>();
    sortedPerDay.forEach(sum -> {
      Duration weeklyDuration = settings.getWorkingDurationPerDay().minus(sum.getDuration());
      remaining.put(sum.getDate(), weeklyDuration);
    });
    model.addAttribute("remaining", remaining);

    return "charts/daily";
  }
}
