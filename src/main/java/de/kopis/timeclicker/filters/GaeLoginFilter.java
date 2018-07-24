package de.kopis.timeclicker.filters;

import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
