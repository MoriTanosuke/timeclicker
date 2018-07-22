package de.kopis.timeclicker.filters;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

@Component
public class GaeLoginFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(GaeLoginFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String requestUrl = ((HttpServletRequest) request).getRequestURL().toString();
        final String path = new URL(requestUrl).getPath();
        if (!path.contains("/_ah/")) {
            LOG.debug("Checking user for request URL {}", requestUrl);
            final UserService userService = UserServiceFactory.getUserService();
            if (userService.getCurrentUser() == null) {
                final String loginURL = userService.createLoginURL("/");
                LOG.debug("User not logged in, redirecting to login URL {}", loginURL);
                ((HttpServletResponse) response).sendRedirect(loginURL);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
