package com.college.erp.collegemanagementsystem.security;

import com.college.erp.collegemanagementsystem.enums.Permission;
import com.college.erp.collegemanagementsystem.enums.UserType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class RolePermissionPolicy {
    private static final Map<UserType, Set<Permission>> PERMISSIONS = buildPermissions();

    private RolePermissionPolicy() { }

    public static Set<SimpleGrantedAuthority> authoritiesFor(UserType userType) {
        if (userType == null) return Set.of();
        return PERMISSIONS.getOrDefault(userType, Set.of()).stream()
                .map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission.name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Map<UserType, Set<Permission>> buildPermissions() {
        Map<UserType, Set<Permission>> permissions = new EnumMap<>(UserType.class);
        Set<Permission> platformAdmin = EnumSet.allOf(Permission.class);
        permissions.put(UserType.SYSTEM_ADMIN, platformAdmin);
        permissions.put(UserType.SUPER_ADMIN, platformAdmin);

        Set<Permission> collegeManagers = EnumSet.of(Permission.ACADEMIC_READ, Permission.ACADEMIC_WRITE);
        permissions.put(UserType.COLLEGE_ADMIN, collegeManagers);
        permissions.put(UserType.COLLEGE_BRANCH, collegeManagers);
        permissions.put(UserType.PRINCIPAL, collegeManagers);

        Set<Permission> academicReaders = EnumSet.of(Permission.ACADEMIC_READ);
        permissions.put(UserType.TEACHER, academicReaders);
        permissions.put(UserType.ACCOUNTANT, academicReaders);
        permissions.put(UserType.LIBRARIAN, academicReaders);
        permissions.put(UserType.STAFF, academicReaders);
        return Map.copyOf(permissions);
    }
}
