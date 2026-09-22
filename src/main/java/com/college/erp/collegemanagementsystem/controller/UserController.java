package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.dto.UserDTO;
import com.college.erp.collegemanagementsystem.enums.UserStatus;
import com.college.erp.collegemanagementsystem.enums.UserType;
import com.college.erp.collegemanagementsystem.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * @author grish
 *
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<RestResponseDTO> getUsers(@RequestParam(required = false, name = "q") String search,
                                                    @RequestParam(required = false) String username,
                                                    @RequestParam(required = false) String fullName,
                                                    @RequestParam(required = false) String email,
                                                    @RequestParam(required = false) String phoneNumber,
                                                    @RequestParam(required = false) Long tenantId,
                                                    @RequestParam(required = false) UserType userType,
                                                    @RequestParam(required = false) Boolean enabled,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                    @RequestParam(required = false) UserStatus status,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        try {
            var users = userService.findPage(search, username, fullName, email, phoneNumber, tenantId, userType, enabled, fromDate, toDate, status, page, size);
            return ResponseEntity.ok(RestResponseDTO.success("Users Found Successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestResponseDTO.failure(e.getMessage()));
        }
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<RestResponseDTO> getUserById(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().body(RestResponseDTO.badRequest("User Id is required"));
        }
        try {
            UserDTO user = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
            return ResponseEntity.ok(RestResponseDTO.success("User Found Successfully", user));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(RestResponseDTO.internalServerError(e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<RestResponseDTO> createUser(@RequestBody UserDTO request,
                                                      @RequestParam(required = false) Long tenantId,
                                                      @RequestParam(required = false) Long userTemplateId) {
        if (request == null) {
            return ResponseEntity.badRequest().body(RestResponseDTO.badRequest("Request could not be empty"));
        }
        try {
            UserDTO response = userService.create(request, tenantId, userTemplateId);
            return new ResponseEntity<>(RestResponseDTO.success("User Created Successfully", response), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(RestResponseDTO.internalServerError(e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<RestResponseDTO> updateUser(@RequestParam Long id,
                                                      @RequestParam(required = false) Long tenantId,
                                                      @RequestParam(required = false) Long userTemplateId,
                                                      @RequestParam(required = false) String remarks,
                                                      @RequestBody UserDTO request) {
        if (id == null) {
            return ResponseEntity.badRequest().body(RestResponseDTO.badRequest("User must be provided."));
        }
        try {
            UserDTO response = userService.update(id, request, tenantId, userTemplateId, remarks);
            return ResponseEntity.ok(RestResponseDTO.success("User Updated Successfully", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(RestResponseDTO.internalServerError(e.getMessage()));
        }
    }

    @PutMapping("/changeStatus")
    public ResponseEntity<RestResponseDTO> changeUserStatus(@RequestParam Long id,
                                                            @RequestParam UserStatus status,
                                                            @RequestParam(required = false) String remarks) {
        if (id == null) {
            return ResponseEntity.badRequest().body(RestResponseDTO.badRequest("User Id is required"));
        }
        if (status == null) {
            return ResponseEntity.badRequest().body(RestResponseDTO.badRequest("Status is required"));
        }
        try {
            UserDTO user = userService.changeStatus(id, status, remarks);
            return ResponseEntity.ok(RestResponseDTO.success("User Status Changed Successfully", user));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(RestResponseDTO.internalServerError(e.getMessage()));
        }
    }
}
