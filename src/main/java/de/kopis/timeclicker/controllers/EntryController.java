package de.kopis.timeclicker.controllers;

import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@Controller
@RequestMapping("/entries")
public class EntryController {
    private TimeclickerAPI api = new TimeclickerAPI();

    private UserService userService = UserServiceFactory.getUserService();

    private final Logger LOG = Logger.getLogger(EntryController.class.getName());

    @GetMapping
    public String index(Model model) throws NotAuthenticatedException {
        // TODO move into request filter and redirect to login automatically
        final User user = userService.getCurrentUser();
        if(user == null) {
            LOG.fine("User not logged in, redirecting to login URL...");
            // quit early
            return userService.createLoginURL("entries");
        }

        final List<TimeEntry> entries = api.list(31, 0, user);
        LOG.fine(() -> String.format("%d entries found", entries.size()));
        model.addAttribute("entries", entries);

        return "entries/index";
    }

    @GetMapping("/add")
    public String create(Model model) {
        // TODO move into request filter and redirect to login automatically
        final User user = userService.getCurrentUser();
        if(user == null) {
            LOG.fine("User not logged in, redirecting to login URL...");
            // quit early
            return userService.createLoginURL("entries/add");
        }

        model.addAttribute("entry", new TimeEntry());

        return "entries/add";
    }

    @PostMapping("/add")
    public String create(@ModelAttribute TimeEntry input) throws NotAuthenticatedException {
        // TODO move into request filter and redirect to login automatically
        final User user = userService.getCurrentUser();
        if(user == null) {
            LOG.fine("User not logged in, redirecting to login URL...");
            return userService.createLoginURL("entries/add");
        }

        api.update(null, input.getStart(), input.getStop(), 0L, input.getDescription(), null, null, user);

        return "entries/index";
    }
}
