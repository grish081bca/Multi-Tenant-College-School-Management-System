package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.UserDTO;
import com.college.erp.collegemanagementsystem.enums.UserStatus;
import com.college.erp.collegemanagementsystem.enums.UserType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * @author grish
 *
 */
public interface UserService {
    PagablePage<UserDTO> findPage(String q, UserStatus status, Integer page, Integer size);

    PagablePage<UserDTO> findPage(String q,
                                  String username,
                                  String fullName,
                                  String email,
                                  String phoneNumber,
                                  Long tenantId,
                                  UserType userType,
                                  Boolean enabled,
                                  LocalDate fromDate,
                                  LocalDate toDate,
                                  UserStatus status,
                                  Integer page,
                                  Integer size);

    List<String> getUsernames();

    UserDTO create(UserDTO userDto, Long tenantId, Long userTemplateId);

    UserDTO update(Long id, UserDTO userDto, Long tenantId, Long userTemplateId, String remarks);

    default UserDTO update(Long id, UserDTO userDto, Long tenantId, Long userTemplateId) {
        return update(id, userDto, tenantId, userTemplateId, null);
    }

    Optional<UserDTO> findById(Long id);

    UserDTO changeStatus(Long id, UserStatus status, String remarks);

    default UserDTO changeStatus(Long id, UserStatus status) {
        return changeStatus(id, status, null);
    }
}
