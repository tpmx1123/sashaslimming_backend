package com.example.slimming.config;

import com.example.slimming.security.JwtAuthenticationEntryPoint;
import com.example.slimming.security.JwtAuthenticationFilter;
import com.example.slimming.security.CustomAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                    // ✅ Allow OPTIONS requests for CORS preflight - MUST be first
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // ✅ Protected Auth Endpoints (must come BEFORE permitAll for /api/auth/**)
                    .requestMatchers(HttpMethod.POST, "/api/auth/change-password").hasRole("ADMIN")
                    // ✅ Public Endpoints - Must be defined first (order matters!)
                    // Explicitly allow all HTTP methods for public endpoints
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/auth/**").permitAll()
                    .requestMatchers("/api/blogs/public/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/blogs/public/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/blogs/public/**").permitAll()
                    .requestMatchers("/api/bookings").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/bookings").permitAll()
                    .requestMatchers("/api/newsletter/subscribe").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/newsletter/subscribe").permitAll()
                    .requestMatchers("/api/contact").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
                    // ✅ Admin Protected Endpoints
                    .requestMatchers("/api/blogs/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/bookings/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/newsletter/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    // ✅ Default: Authentication required
                    .anyRequest().authenticated()
            )
            // ✅ Allow anonymous access for public endpoints (default is enabled)
            // ✅ Handle unauthorized (401) and forbidden (403) access - ensure CORS headers are included
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler))
            // ✅ Stateless JWT sessions
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // ✅ Register authentication provider & JWT filter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",       // React default port
                "http://localhost:3001",       // Alternative React port
                "http://localhost:5173"       // ✅ Non-www version
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
