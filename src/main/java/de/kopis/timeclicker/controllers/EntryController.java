package de.kopis.timeclicker.controllers;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.Application;
import de.kopis.timeclicker.api.TimeclickerAPI;
import de.kopis.timeclicker.exceptions.NotAuthenticatedException;
import de.kopis.timeclicker.model.TimeEntry;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

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
        if (user == null) {
            LOG.fine("User not logged in, redirecting to login URL...");
            // quit early
            return "redirect:" + userService.createLoginURL("/entries");
        } else {

            final List<TimeEntry> entries = api.list(31, 0, user);
            LOG.fine(() -> String.format("%d entries found", entries.size()));
            model.addAttribute("entries", entries);
        }

        return "entries/index";
    }

    @GetMapping("/add")
    public String create(Model model) {
        // TODO move into request filter and redirect to login automatically
        final User user = userService.getCurrentUser();
        if (user == null) {
            LOG.fine("User not logged in, redirecting to login URL...");
            // quit early
            return "redirect:" + userService.createLoginURL("/entries/add");
        }

        model.addAttribute("entry", new TimeEntry());

        return "entries/add";
    }

    @PostMapping("/add")
    public String create(@ModelAttribute TimeEntry input) throws NotAuthenticatedException {
        // TODO move into request filter and redirect to login automatically
        final User user = userService.getCurrentUser();
        if (user == null) {
            LOG.fine("User not logged in, redirecting to login URL...");
            return "redirect:" + userService.createLoginURL("/entries/add");
        }

        api.update(null, input.getStart(), input.getStop(), 0L, input.getDescription(), null, null, user);

        return "entries";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(Application.DATE_FORMAT, true, 19));
    }
}
