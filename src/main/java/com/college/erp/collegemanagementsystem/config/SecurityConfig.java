package com.college.erp.collegemanagementsystem.config;

import com.college.erp.collegemanagementsystem.security.CustomUserDetailsService;
import com.college.erp.collegemanagementsystem.security.WebLoginSuccessHandler;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * @author grish
 *
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService service, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(service);
        provider.setPasswordEncoder(encoder);
        return provider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DaoAuthenticationProvider provider,
                                                   WebLoginSuccessHandler loginSuccessHandler) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(provider)
                .exceptionHandling(exception -> exception.defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"), request -> request.getRequestURI().startsWith(request.getContextPath() + "/web/")))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/", "/login", "/login/**", "/error", "/css/**", "/js/**", "/api/auth/register", "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login").usernameParameter("username").successHandler(loginSuccessHandler).failureUrl("/login?error=true").permitAll())
                .logout(logout -> logout.logoutUrl("/web/logout").logoutSuccessUrl("/login?logout=true"))
                .build();
    }
}
