package worldcupbraket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

@Configuration
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RememberMeServices rememberMeServices
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/", "/signup", "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/", "/signup", "/signup/", "/login", "/login/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/results", "/results/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.sendRedirect("/")
                        )
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        response.sendRedirect("/")
                        )
                )
                .rememberMe(remember -> remember
                        .rememberMeServices(rememberMeServices)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("JSESSIONID", "worldcup-remember-me")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .build();
    }

    @Bean
    public RememberMeServices rememberMeServices(
            @Value("${app.remember-me.key}") String rememberMeKey,
            @Value("${app.remember-me.use-secure-cookie:false}") boolean useSecureCookie,
            UserDetailsService userDetailsService
    ) {
        TokenBasedRememberMeServices services =
                new TokenBasedRememberMeServices(rememberMeKey, userDetailsService);

        services.setCookieName("worldcup-remember-me");
        services.setTokenValiditySeconds(60 * 60 * 24 * 90);
        services.setAlwaysRemember(true);
        services.setUseSecureCookie(useSecureCookie);

        return services;
    }
}