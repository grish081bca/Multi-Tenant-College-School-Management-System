package com.college.erp.collegemanagementsystem.security;

import com.college.erp.collegemanagementsystem.entity.User;
import com.college.erp.collegemanagementsystem.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author grish
 *
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toPrincipal(user);
    }
    @Transactional(readOnly = true)
    public AuthenticatedUserPrincipal loadByTenantIdAndUsername(Long tenantId, String username) {
        User user = userRepository.findByTenant_IdAndUsernameIgnoreCase(tenantId, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toPrincipal(user);
    }
    @Transactional(readOnly = true)
    public AuthenticatedUserPrincipal loadByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toPrincipal(user);
    }

    private AuthenticatedUserPrincipal toPrincipal(User user) {
        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        if (user.getUserType() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()));
            authorities.add(new SimpleGrantedAuthority("USER_TYPE_" + user.getUserType().name()));
            authorities.addAll(RolePermissionPolicy.authoritiesFor(user.getUserType()));
        }

        return AuthenticatedUserPrincipal.fromUser(user, authorities);
    }
}
