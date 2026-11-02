package com.college.erp.collegemanagementsystem.util;

/**
 * @author grish
 *
 */
public final class ConvertUtils {
    public ConvertUtils() {
    }

    public static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String finalValue = value.trim();
        return finalValue.isEmpty() ? null : finalValue;
    }

    public static String normalizeToLowerCase(String value) {
        String normalized = normalizeText(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    public static String normalizeToUppercase(String value) {
        String normalized = normalizeText(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    // Conversion helpers for User entity <-> UserDTO
    public static com.college.erp.collegemanagementsystem.dto.UserDTO toUserDTO(com.college.erp.collegemanagementsystem.entity.User user) {
        if (user == null) return null;
        com.college.erp.collegemanagementsystem.dto.UserDTO dto = new com.college.erp.collegemanagementsystem.dto.UserDTO();
        dto.setId(user.getId());
        if (user.getCreatedAt() != null) {
            dto.setCreatedDate(DateFormatUtils.formatDateTime(user.getCreatedAt()));
        }
        if (user.getUpdatedAt() != null) {
            dto.setUpdatedDate(DateFormatUtils.formatDateTime(user.getUpdatedAt()));
        }
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setFirstName(user.getFirstName());
        dto.setMiddleName(user.getMiddleName());
        dto.setLastName(user.getLastName());
        // Do not expose password hash in DTO for security; leave null
        dto.setPassword(null);
        dto.setStatus(user.getStatus());
        dto.setEnabled(user.isEnabled());
        dto.setUserType(user.getUserType());
        if (user.getTenant() != null) {
            dto.setTenantId(user.getTenant().getId());
            dto.setTenantName(user.getTenant().getTenantName());
        }
        if (user.getUserTemplate() != null) {
            dto.setUserTemplateId(user.getUserTemplate().getId());
            dto.setUserTemplateName(user.getUserTemplate().getUserTemplateName());
        }
        return dto;
    }

    public static java.util.List<com.college.erp.collegemanagementsystem.dto.UserDTO> toUserDTOList(java.util.List<com.college.erp.collegemanagementsystem.entity.User> users) {
        java.util.List<com.college.erp.collegemanagementsystem.dto.UserDTO> list = new java.util.ArrayList<>();
        if (users == null || users.isEmpty()) return list;
        for (com.college.erp.collegemanagementsystem.entity.User u : users) {
            list.add(toUserDTO(u));
        }
        return list;
    }
}
