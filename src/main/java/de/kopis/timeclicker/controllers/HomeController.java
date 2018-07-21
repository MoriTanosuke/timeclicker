package de.kopis.timeclicker.controllers;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.logging.Logger;

@Controller
@RequestMapping("/")
public class HomeController {
    private final Logger LOG = Logger.getLogger(HomeController.class.getName());

    private TimeclickerAPI api = new TimeclickerAPI();

    private UserService userService = UserServiceFactory.getUserService();

    @GetMapping()
    public String home(Model model) throws NotAuthenticatedException {
        // TODO move into request filter and redirect to login automatically
        final User user = userService.getCurrentUser();
        if (user == null) {
            LOG.fine("User not logged in, redirecting to login URL...");
            // quit early
            return "redirect:" + userService.createLoginURL("/entries");
        }

        final TimeEntry latest = api.latest(user);
        if (latest != null) {
            model.addAttribute("entry", latest);
        }

        return "index";
    }
}
