package foundIT.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Provides the password encoder used for hashing and verifying admin passwords. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    
    @Bean

    /** Disables interactive auth flows and leaves all routes open to the application itself. */
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
    .csrf(csrf -> csrf.disable())
    .httpBasic(httpBasic -> httpBasic.disable())
    .formLogin(form -> form.disable())
    .authorizeHttpRequests(auth -> auth
        .anyRequest().permitAll()
    );

        return http.build();
    }
}
