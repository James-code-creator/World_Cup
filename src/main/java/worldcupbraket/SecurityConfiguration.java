package worldcupbraket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
                .logout(logout -> logout.permitAll())
                .build();
    }
}