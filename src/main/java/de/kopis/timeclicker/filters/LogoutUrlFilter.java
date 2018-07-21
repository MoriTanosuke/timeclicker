package de.kopis.timeclicker.filters;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class LogoutUrlFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final UserService userService = UserServiceFactory.getUserService();
        if (userService.getCurrentUser() != null) {
            ((HttpServletRequest) request).getSession(true).setAttribute("logoutUrl", userService.createLogoutURL("/"));
        } else {
            ((HttpServletRequest) request).getSession().removeAttribute("logoutUrl");
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
