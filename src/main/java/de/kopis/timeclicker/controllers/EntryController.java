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
    public String list(Model model) throws NotAuthenticatedException {
        // TODO move into request filter and redirect to login automatically
        final User user = userService.getCurrentUser();
        if (user == null) {
            LOG.fine("User not logged in, redirecting to login URL...");
            return "redirect:" + userService.createLoginURL("/entries");
        }

        final List<TimeEntry> entries = api.list(31, 0, user);
        model.addAttribute("entries", entries);

        return "entries/list";
    }

    @PostMapping
    public String create(@ModelAttribute TimeEntry input) throws NotAuthenticatedException {
        // TODO move into request filter and redirect to login automatically
        final User user = userService.getCurrentUser();
        if (user == null) {
            LOG.fine("User not logged in, redirecting to login URL...");
            return "redirect:" + userService.createLoginURL("/entries/add");
        }

        api.update(input.getKey(),
                input.getStart(), input.getStop(), 0L,
                input.getDescription(), input.getTags(), input.getProject(),
                user);

        return "redirect:/entries/list";
    }

    @GetMapping("/{key}")
    public String get(Model model, @PathVariable String key) throws NotAuthenticatedException {
        // TODO move into request filter and redirect to login automatically
        final User user = userService.getCurrentUser();
        if (user == null) {
            LOG.fine("User not logged in, redirecting to login URL...");
            // quit early
            return "redirect:" + userService.createLoginURL("/entries");
        }

        final TimeEntry entry = api.show(key, user);
        if (entry != null) {
            model.addAttribute("entry", entry);
        }

        return "entries/add";
    }

    @DeleteMapping("/{key}")
    public String delete(@PathVariable String key) throws NotAuthenticatedException {
        // TODO move into request filter and redirect to login automatically
        final User user = userService.getCurrentUser();
        if (user == null) {
            LOG.fine("User not logged in, redirecting to login URL...");
            return "redirect:" + userService.createLoginURL("/entries/list");
        }

        api.delete(key, user);

        return "redirect:/entries/list";
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

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(Application.DATE_FORMAT, true, 19));
    }
}
