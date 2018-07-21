package de.kopis.timeclicker.filters;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;

@Component
public class GaeAuthenticationFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(GaeAuthenticationFilter.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOG.debug("filtering method={} url={}", ((HttpServletRequest) request).getMethod(), ((HttpServletRequest) request).getRequestURL().toString());

        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        LOG.debug("user {}", user);

        if (user != null) {
            PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(user, null, Arrays.asList(new SimpleGrantedAuthority("USER")));
            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            SecurityContextHolder.getContext().setAuthentication(null);
        }


        LOG.debug("Continuing filter chain");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
