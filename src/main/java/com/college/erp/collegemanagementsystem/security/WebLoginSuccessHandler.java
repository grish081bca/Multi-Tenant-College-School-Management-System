package com.college.erp.collegemanagementsystem.security;

import com.college.erp.collegemanagementsystem.entity.User;
import com.college.erp.collegemanagementsystem.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class WebLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public WebLoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal) {
            User user = userRepository.findById(principal.getUserId()).orElse(null);
            if (user != null) {
                user.setLastLoginAt(OffsetDateTime.now());
                userRepository.saveAndFlush(user);
            }
        }
        response.sendRedirect(request.getContextPath() + "/web/dashboard");
    }
}
