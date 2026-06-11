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
                    .requestMatchers(HttpMethod.GET, "/", "/signup").permitAll()
                    .requestMatchers(HttpMethod.POST, "/", "/signup", "/signup/").permitAll()
                    .requestMatchers("/results", "/results/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .logout(logout -> logout.permitAll())
            .build();
    }
}