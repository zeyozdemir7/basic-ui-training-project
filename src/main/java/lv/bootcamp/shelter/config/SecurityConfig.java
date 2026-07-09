package lv.bootcamp.shelter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Baseline Spring Security setup for the shelter starter project.
 *
 * <ul>
 *   <li>Two in-memory demo accounts, with deliberately separate roles:
 *       {@code user}/{@code user123} (ROLE_USER only) and
 *       {@code admin}/{@code admin123} (ROLE_ADMIN only).</li>
 *   <li>Anyone (including anonymous visitors) can browse the animal pages
 *       and read the API (GET).</li>
 *   <li>Only ROLE_ADMIN can create animals ({@code POST /animals},
 *       {@code POST /api/animals}).</li>
 *   <li>Only ROLE_USER can adopt an animal ({@code POST /api/animals/{id}/adopt});
 *       admins are deliberately excluded since they don't have ROLE_USER.</li>
 *   <li>Only ROLE_ADMIN can list adopted animals ({@code GET /api/animals/adopted}) —
 *       a read-only endpoint, handy for testing role-based JWT authorization (bonus
 *       task below) without any side effects.</li>
 *   <li>Only ROLE_ADMIN sees the "adopted by {userId} on {date}" note —
 *       enforced in {@code AnimalService#toResponse}, not here, since it's a
 *       field-level (not URL-level) restriction.</li>
 *   <li>Uses Spring Security's built-in default login page (served at
 *       {@code /login}) — nothing to build here yet.</li>
 * </ul>
 *
 * <p><b>Bonus task (web):</b> replace the default login page with your own:
 * <ol>
 *   <li>Create {@code src/main/resources/templates/login.html} (requires
 *       Thymeleaf from Task A) with a form posting username/password to
 *       {@code /login}.</li>
 *   <li>Point Spring Security at it: {@code formLogin(form -> form.loginPage("/login").permitAll())}.</li>
 *   <li>Add a link to it from the nav bar, and a logout button/form that
 *       posts to {@code /logout}.</li>
 * </ol>
 *
 * <p><b>Bonus task (API/Swagger):</b> swap HTTP Basic on {@code /api/**} for JWT,
 * so Swagger UI (Task B) can authorize with a bearer token instead of a
 * username/password prompt on every request:
 * <ol>
 *   <li>Uncomment the {@code jjwt-*} dependencies in {@code pom.xml}.</li>
 *   <li>Add a {@code POST /api/auth/login} endpoint that takes a
 *       username/password, authenticates via {@code AuthenticationManager},
 *       and returns a signed JWT (subject = username, include roles as a
 *       claim, short expiry).</li>
 *   <li>Write a {@code JwtAuthFilter} ({@code OncePerRequestFilter}) that reads
 *       the {@code Authorization: Bearer <token>} header, validates/parses the
 *       JWT, and sets the {@code Authentication} in the
 *       {@code SecurityContextHolder} — add it before
 *       {@code UsernamePasswordAuthenticationFilter} in the filter chain.</li>
 *   <li>Make the API stateless: {@code sessionManagement(session ->
 *       session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))} for
 *       {@code /api/**}, and drop {@code httpBasic()} once the filter works.</li>
 *   <li>In Springdoc/Swagger UI, configure an HTTP bearer security scheme so
 *       the "Authorize" button sends {@code Authorization: Bearer <token>}.</li>
 * </ol>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // The JSON API is exercised directly with HTTP Basic (curl/Postman/Swagger),
                // which never carries an ambient browser session cookie, so CSRF tokens
                // aren't needed there. Browser-facing pages/forms keep CSRF protection.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,
                                "/", "/", "/*.html", "/css/**", "/js/**", "/images/**", "/favicon.ico",
                                "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/animals/adopted").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/animals/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/animals/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/animals", "/animals/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/animals/*/adopt").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/animals").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/animals").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .logout(Customizer.withDefaults());

        return http.build();
    }
}
