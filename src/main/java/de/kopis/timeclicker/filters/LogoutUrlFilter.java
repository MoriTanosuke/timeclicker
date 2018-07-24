package de.kopis.timeclicker.filters;

import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

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
