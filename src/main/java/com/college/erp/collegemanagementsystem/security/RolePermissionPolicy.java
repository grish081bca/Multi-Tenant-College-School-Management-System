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

    private RolePermissionPolicy() {
    }

    public static Set<SimpleGrantedAuthority> authoritiesFor(UserType userType) {
        if (userType == null) return Set.of();
        return PERMISSIONS.getOrDefault(userType, Set.of()).stream()
                .map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission.name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Map<UserType, Set<Permission>> buildPermissions() {
        Map<UserType, Set<Permission>> permissions = new EnumMap<>(UserType.class);
        Set<Permission> superAdmin = EnumSet.allOf(Permission.class);
        // These routes are tied to the authenticated person's own student/teacher profile.
        superAdmin.removeAll(EnumSet.of(Permission.STUDENT_PORTAL, Permission.TEACHER_PORTAL));
        permissions.put(UserType.SUPER_ADMIN, superAdmin);

        Set<Permission> systemAdmin = EnumSet.allOf(Permission.class);
        systemAdmin.removeAll(EnumSet.of(Permission.ACADEMIC_WRITE, Permission.ADMISSION_WRITE, Permission.EMPLOYEE_WRITE,
                Permission.ATTENDANCE_WRITE, Permission.TIMETABLE_WRITE, Permission.EXAMINATION_WRITE,
                Permission.MARKS_WRITE, Permission.MARKS_VERIFY, Permission.RESULTS_PROCESS,
                Permission.CERTIFICATE_WRITE, Permission.FEE_STRUCTURE_READ, Permission.FEE_STRUCTURE_WRITE,
                Permission.FEE_COLLECTION_READ, Permission.FEE_COLLECTION_WRITE, Permission.NOTICE_READ,
                Permission.NOTICE_WRITE, Permission.STUDENT_PORTAL, Permission.TEACHER_PORTAL));
        permissions.put(UserType.SYSTEM_ADMIN, systemAdmin);

        Set<Permission> collegeManagers = EnumSet.of(Permission.ACADEMIC_READ, Permission.ACADEMIC_WRITE,
                Permission.ADMISSION_READ, Permission.ADMISSION_WRITE, Permission.EMPLOYEE_READ, Permission.EMPLOYEE_WRITE,
                Permission.ATTENDANCE_READ, Permission.ATTENDANCE_WRITE, Permission.TIMETABLE_READ, Permission.TIMETABLE_WRITE,
                Permission.EXAMINATION_READ, Permission.EXAMINATION_WRITE, Permission.MARKS_READ,
                Permission.MARKS_WRITE, Permission.MARKS_VERIFY, Permission.RESULTS_READ, Permission.RESULTS_PROCESS,
                Permission.CERTIFICATE_READ, Permission.CERTIFICATE_WRITE, Permission.FEE_STRUCTURE_READ,
                Permission.FEE_STRUCTURE_WRITE, Permission.FEE_COLLECTION_READ, Permission.FEE_COLLECTION_WRITE,
                Permission.NOTICE_READ, Permission.NOTICE_WRITE);
        permissions.put(UserType.COLLEGE_ADMIN, collegeManagers);
        permissions.put(UserType.COLLEGE_BRANCH, collegeManagers);
        permissions.put(UserType.PRINCIPAL, collegeManagers);

        Set<Permission> academicReaders = EnumSet.of(Permission.ACADEMIC_READ);
        permissions.put(UserType.TEACHER, EnumSet.of(Permission.ACADEMIC_READ, Permission.EMPLOYEE_READ,
                Permission.ATTENDANCE_READ, Permission.TIMETABLE_READ, Permission.EXAMINATION_READ,
                Permission.MARKS_READ, Permission.RESULTS_READ));
        permissions.get(UserType.TEACHER).add(Permission.TEACHER_PORTAL);
        permissions.put(UserType.ACCOUNTANT, EnumSet.of(Permission.ACADEMIC_READ, Permission.FEE_STRUCTURE_READ,
                Permission.FEE_COLLECTION_READ, Permission.FEE_COLLECTION_WRITE));
        permissions.put(UserType.LIBRARIAN, academicReaders);
        permissions.put(UserType.STUDENT, EnumSet.of(Permission.STUDENT_PORTAL));
        permissions.put(UserType.STAFF, EnumSet.of(Permission.ACADEMIC_READ, Permission.EMPLOYEE_READ, Permission.ADMISSION_READ));
        return Map.copyOf(permissions);
    }
}
