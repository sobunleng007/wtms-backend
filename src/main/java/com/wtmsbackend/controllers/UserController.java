package com.wtmsbackend.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.wtmsbackend.dto.ApiResponse;
import com.wtmsbackend.dto.request.UserRequest;
import com.wtmsbackend.dto.request.UserUpdateRequest;
import com.wtmsbackend.dto.response.UserResponse;
import com.wtmsbackend.models.User;
import com.wtmsbackend.models.Until.Role;
import com.wtmsbackend.services.CloudinaryImageService;
import com.wtmsbackend.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

  private final UserService userService;
  private final CloudinaryImageService cloudinaryImageService;

  // 1. Get all users with Pagination (ADMIN ONLY)
  @Operation(
      summary = "Get all users (Paginated)",
      description =
          "Retrieves a paginated list of all users. This endpoint is restricted to users with the ADMIN role.")
  @GetMapping
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<?> getAllUsers() {
    List<UserResponse> users = userService.getAllUsers();
    ApiResponse<List<UserResponse>> response =
        ApiResponse.<List<UserResponse>>builder()
            .message("Users fetched successfully")
            .success(true)
            .payload(users)
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.ok(response);
  }

  // 2. Get current logged-in user

  @Operation(summary = "Get current authenticated user")
  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User currentUser) {

    // Use mapToUserResponse style — include role and phoneNumber
    UserResponse userResponse =
        UserResponse.builder()
            .id(currentUser.getId())
            .firstName(currentUser.getFirstName())
            .lastName(currentUser.getLastName())
            .email(currentUser.getEmail())
            .imageUrl(currentUser.getImageUrl())
            .phoneNumber(currentUser.getPhoneNumber())
            .address(currentUser.getAddress())
            .status(currentUser.getStatus())
            .role(currentUser.getRole() != null ? currentUser.getRole().name() : null)
            .departmentId(
                currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null)
            .departmentName(
                currentUser.getDepartment() != null ? currentUser.getDepartment().getName() : null)
            .build();

    return ResponseEntity.ok(
        ApiResponse.<UserResponse>builder()
            .message("Current user fetched successfully")
            .success(true)
            .payload(userResponse)
            .timestamp(LocalDateTime.now())
            .build());
  }

  // 3. Get user by ID
  @Operation(
      summary = "Get user by ID",
      description = "Retrieves a specific user's details by their unique ID.")
  @GetMapping("/{id}")
  public ResponseEntity<?> getUserById(@PathVariable Integer id) {
    UserResponse user = userService.getUserById(id);
    ApiResponse<UserResponse> response =
        ApiResponse.<UserResponse>builder()
            .message("User fetched successfully")
            .success(true)
            .payload(user)
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.ok(response);
  }

  // 4. Get user by Email
  @Operation(
      summary = "Get user by Email",
      description = "Retrieves a specific user's details by their registered email address.")
  @GetMapping("/email/{email}")
  public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
    UserResponse user = userService.getUserByEmail(email);
    ApiResponse<UserResponse> response =
        ApiResponse.<UserResponse>builder()
            .message("User fetched successfully")
            .success(true)
            .payload(user)
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.ok(response);
  }

  // 5. Create user with image upload (multipart/form-data)
  @Operation(
      summary = "Create a new user with image upload",
      description =
          "Registers a new user in the system with image upload to Cloudinary. Send as multipart/form-data with 'request' (JSON string) and optional 'image' (file).")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> createUserWithImage(
      @RequestPart("request") String requestJson,
      @RequestPart(value = "image", required = false) MultipartFile image) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      UserRequest request = objectMapper.readValue(requestJson, UserRequest.class);

      if (image != null && !image.isEmpty()) {
        try {
          Map<String, Object> uploadResult = cloudinaryImageService.upload(image);
          request.setImageUrl((String) uploadResult.get("secure_url"));
        } catch (IOException e) {
          throw new RuntimeException("Image upload failed!", e);
        }
      }
      UserResponse user = userService.createUser(request);
      ApiResponse<UserResponse> response =
          ApiResponse.<UserResponse>builder()
              .message("User created successfully")
              .success(true)
              .payload(user)
              .timestamp(LocalDateTime.now())
              .build();
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse request: " + e.getMessage(), e);
    }
  }

  // 5b. Create user with JSON (application/json)
  @Operation(
      summary = "Create a new user with JSON",
      description =
          "Registers a new user in the system with JSON payload. No image upload support. Send as application/json.")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest request) {
    try {
      UserResponse user = userService.createUser(request);
      ApiResponse<UserResponse> response =
          ApiResponse.<UserResponse>builder()
              .message("User created successfully")
              .success(true)
              .payload(user)
              .timestamp(LocalDateTime.now())
              .build();
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
    }
  }

  // 6. Update user (No password update)
  @Operation(
      summary = "Update user details",
      description =
          "Updates an existing user's profile information. Note: This endpoint does not update the user's password.")
  @PutMapping("/{id}")
  public ResponseEntity<?> updateUser(
      @PathVariable Integer id, @Valid @RequestBody UserUpdateRequest request) {

    UserResponse user = userService.updateUser(id, request);
    ApiResponse<UserResponse> response =
        ApiResponse.<UserResponse>builder()
            .message("User updated successfully")
            .success(true)
            .payload(user)
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.ok(response);
  }

  // 7. Soft Delete User
  @Operation(
      summary = "Deactivate (Soft Delete) user",
      description =
          "Deactivates a user by setting their status to false instead of permanently removing them from the database.")
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
    userService.deleteUser(id);
    ApiResponse<Void> response =
        ApiResponse.<Void>builder()
            .message("User deleted (deactivated) successfully")
            .success(true)
            .payload(null)
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.ok(response);
  }

  // 8. Admin Reset Password by ID
  @Operation(
      summary = "Admin reset user password",
      description =
          "Allows an Admin to directly override and reset a specific user's password using their ID. Restricted to ADMIN role.")
  @PutMapping("/{id}/reset-password")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<?> userResetPassword(
      @PathVariable Integer id,
      @Valid @RequestBody com.wtmsbackend.dto.request.UserResetPasswordRequest request) {

    User user = userService.getUserEntityById(id); // Fetch the user entity by ID
    userService.userResetPassword(user, request.getOldPassword(), request.getNewPassword());

    ApiResponse<Void> response =
        ApiResponse.<Void>builder()
            .message("User password reset successfully by Admin")
            .success(true)
            .payload(null)
            .timestamp(java.time.LocalDateTime.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get users by Department ID",
      description = "Retrieves a list of users that belong to a specific department.")
  @GetMapping("/department/{departmentId}")
  public ResponseEntity<?> getUsersByDepartment(@PathVariable Integer departmentId) {
    List<UserResponse> users = userService.getUsersByDepartment(departmentId);
    ApiResponse<List<UserResponse>> response =
        ApiResponse.<List<UserResponse>>builder()
            .message("Users fetched successfully by department")
            .success(true)
            .payload(users)
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PutMapping("/{id}/{role}")
  @PreAuthorize("hasAnyAuthority('ADMIN')")
  public ResponseEntity<?> updateRoleUser(@PathVariable Integer id, @PathVariable Role role) {

    UserResponse user = userService.updateRole(id, role);
    ApiResponse<UserResponse> response =
        ApiResponse.<UserResponse>builder()
            .message("User updated successfully")
            .success(true)
            .payload(user)
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get users by Role",
      description = "Retrieves a list of users that have a specific role.")
  @GetMapping("/role/{role}")
  public ResponseEntity<?> getUsersByRole(@PathVariable Role role) {
    List<UserResponse> users = userService.getUsersByRole(role);
    ApiResponse<List<UserResponse>> response =
        ApiResponse.<List<UserResponse>>builder()
            .message("Users fetched successfully by role")
            .success(true)
            .payload(users)
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.ok(response);
  }
}
