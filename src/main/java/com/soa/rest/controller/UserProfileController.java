package com.soa.rest.controller;

import com.soa.rest.model.UserProfile;
import com.soa.rest.repository.UserProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST User Profile Controller.
 *
 * API endpoints (лабын шаардлагын дагуу):
 *   POST   /users        – Create profile
 *   GET    /users        – Read all profiles
 *   GET    /users/:id    – Read profile by ID
 *   PUT    /users/:id    – Update profile
 *   DELETE /users/:id    – Delete profile
 *
 * Бүх endpoint AuthMiddleware-ээр хамгаалагдсан.
 */
@RestController
@RequestMapping("/users")
public class UserProfileController {

    private final UserProfileRepository repo;

    public UserProfileController(UserProfileRepository repo) {
        this.repo = repo;
    }

    // ── POST /users – Create ──────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody UserProfile profile,
                                           HttpServletRequest request) {
        Long authUserId = (Long) request.getAttribute("authUserId");

        // authUserId-г profile-д холбох
        if (authUserId != null) {
            profile.setAuthUserId(authUserId);
        }

        // Email давхардал шалгах
        if (profile.getEmail() != null && repo.existsByEmail(profile.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already exists"));
        }

        UserProfile saved = repo.save(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── GET /users – Read all ─────────────────────────────────
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllProfiles() {
        return ResponseEntity.ok(repo.findAll());
    }

    // ── GET /users/:id – Read by ID ───────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        Optional<UserProfile> opt = repo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found with id: " + id));
        }
        return ResponseEntity.ok(opt.get());
    }

    // ── PUT /users/:id – Update ───────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id,
                                           @RequestBody UserProfile updated) {
        Optional<UserProfile> opt = repo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found with id: " + id));
        }

        UserProfile existing = opt.get();

        // Null биш талбаруудыг л шинэчлэх
        if (updated.getName()      != null) existing.setName(updated.getName());
        if (updated.getEmail()     != null) existing.setEmail(updated.getEmail());
        if (updated.getPhone()     != null) existing.setPhone(updated.getPhone());
        if (updated.getBio()       != null) existing.setBio(updated.getBio());
        if (updated.getAvatarUrl() != null) existing.setAvatarUrl(updated.getAvatarUrl());

        return ResponseEntity.ok(repo.save(existing));
    }

    // ── DELETE /users/:id – Delete ────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found with id: " + id));
        }
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
