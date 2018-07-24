package de.kopis.timeclicker.controllers;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSumWithDate;
import de.kopis.timeclicker.utils.TimeSumUtility;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@Controller
@RequestMapping("/charts")
public class ChartController {
  private final TimeclickerAPI api = new TimeclickerAPI();
  private final UserService userService = UserServiceFactory.getUserService();

  @GetMapping("/weekly")
  public String getDailyChart(Model model) throws NotAuthenticatedException {
    final User user = userService.getCurrentUser();

    // 12 weeks
    final List<TimeEntry> allEntries = api.list(31 * 12, 0, user);
    final List<TimeSumWithDate> sortedPerWeek = new TimeSumUtility().calculateWeeklyTimeSum(allEntries);
    model.addAttribute("weeklySums", sortedPerWeek);

    return "charts/weekly";
  }
}
