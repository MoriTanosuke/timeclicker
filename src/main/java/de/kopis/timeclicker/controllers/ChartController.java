package de.kopis.timeclicker.controllers;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.EntryNotOwnedByUserException;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TagSummary;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSumWithDate;
import de.kopis.timeclicker.model.UserSettings;
import de.kopis.timeclicker.utils.TimeSumUtility;
import de.kopis.timeclicker.utils.WorkdayCalculator;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/charts")
public class ChartController {
  private final TimeclickerAPI api = new TimeclickerAPI();
  private final UserService userService = UserServiceFactory.getUserService();
  private static final TimeSumUtility TIME_SUM_UTILITY = new TimeSumUtility();

  @GetMapping("/monthly")
  public String getMonthlyChart(Model model,
                                @RequestParam(defaultValue = "31") int limit,
                                @RequestParam(defaultValue = "0") int page) throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final User user = userService.getCurrentUser();

    final List<TimeEntry> allEntries = api.list(null, limit, page, user);
    final List<TimeSumWithDate> sortedPerMonth = TIME_SUM_UTILITY.calculateMonthlyTimeSum(allEntries);
    sortedPerMonth.sort(Comparator.comparing(TimeSumWithDate::getDate));
    model.addAttribute("monthlySums", sortedPerMonth);

    final UserSettings settings = api.getUserSettings(null, user);
    final Map<Date, Duration> remaining = new TreeMap<>();
    sortedPerMonth.forEach(sum -> {
      final Instant startDate = sum.getDate().toInstant();
      final Calendar cal = Calendar.getInstance();
      // next month
      cal.add(Calendar.MONTH, 1);
      // previous day
      cal.add(Calendar.DAY_OF_YEAR, -1);
      // should be last day of the month
      final Instant stopDate = cal.toInstant();
      final int workingDays = WorkdayCalculator.getWorkingDays(startDate, stopDate);
      //TODO should i calculate the real amount of working days for each month?
      // currently there is the hardcoded assumption that the user works 5 days a week, 4 weeks per month
      Duration monthlyDuration = settings.getWorkingDurationPerDay().multipliedBy(workingDays).minus(sum.getDuration());
      remaining.put(sum.getDate(), monthlyDuration);
    });
    model.addAttribute("remaining", remaining);

    return "charts/monthly";
  }

  @GetMapping("/weekly")
  public String getWeeklyChart(Model model,
                               @RequestParam(defaultValue = "31") int limit,
                               @RequestParam(defaultValue = "0") int page) throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final User user = userService.getCurrentUser();

    final List<TimeEntry> allEntries = api.list(null, limit, page, user);
    final List<TimeSumWithDate> sortedPerWeek = TIME_SUM_UTILITY.calculateWeeklyTimeSum(allEntries);
    sortedPerWeek.sort(Comparator.comparing(TimeSumWithDate::getDate));
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
                              @RequestParam(defaultValue = "0") int page) throws NotAuthenticatedException, EntryNotOwnedByUserException {
    final User user = userService.getCurrentUser();

    final List<TimeEntry> allEntries = api.list(null, limit, page, user);
    final List<TimeSumWithDate> sortedPerDay = new TimeSumUtility().calculateDailyTimeSum(allEntries);
    sortedPerDay.sort(Comparator.comparing(TimeSumWithDate::getDate));
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

    @GetMapping("/tag")
    public String getSumPerTagChart(Model model,
                                    @RequestParam(defaultValue = "31") int limit,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) throws NotAuthenticatedException {
        final User user = userService.getCurrentUser();

        final Collection<TagSummary> allEntries;
        if (since != null) {
            ZoneId zone;
            try {
                final UserSettings settings = api.getUserSettings(null, user);
                zone = ZoneId.of(settings.getTimezone().getID());
            } catch (EntryNotOwnedByUserException e) {
                zone = ZoneId.systemDefault();
            }
            Date date = Date.from(since.atStartOfDay(zone).toInstant());
            allEntries = api.getSummaryForTagsSince(date, user);
        } else {
            allEntries = api.getSummaryForTags(limit, page, user);
        }
        model.addAttribute("tagSummary", allEntries);

        return "charts/tag";
    }
}
