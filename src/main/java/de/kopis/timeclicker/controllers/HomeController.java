package de.kopis.timeclicker.controllers;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import de.kopis.timeclicker.model.TimeSum;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

        return "index";
    }
}
