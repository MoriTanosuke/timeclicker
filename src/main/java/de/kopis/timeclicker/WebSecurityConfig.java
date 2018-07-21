package de.kopis.timeclicker;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import de.kopis.timeclicker.filters.GaeAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Autowired
    private GaeAuthenticationFilter gaeAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        UserService userService = UserServiceFactory.getUserService();
        http
                .csrf().ignoringAntMatchers("/_ah/**")
                .and()
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .authorizeRequests().antMatchers("/_ah/login", "/_ah/logout").permitAll()
                .and()
                .formLogin().loginPage(userService.createLoginURL("/")).permitAll()
                .and()
                .logout().logoutUrl(userService.createLogoutURL("/")).permitAll();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new AuthenticationManager() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                LOG.debug("authenticating principal {}", authentication.getPrincipal());
                User user = UserServiceFactory.getUserService().getCurrentUser();
                if (user.equals(authentication.getPrincipal())) {
                    authentication.setAuthenticated(true);
                }
                return authentication;
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        LOG.debug("failing");
        return new SimpleUrlAuthenticationFailureHandler("/");
    }
}
