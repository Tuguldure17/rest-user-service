package com.soa.rest.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Profile DB-д хадгалагдах хэрэглэгчийн профайл мэдээлэл.
 * authUserId нь SOAP Auth Service-ийн AuthUser.id-тэй холбогдоно.
 */
@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SOAP Auth Service-ийн user ID (foreign key биш, зөвхөн reference)
    @Column(unique = true)
    private Long authUserId;

    @Column(length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String bio;

    // Lab 7: DigitalOcean Spaces-ийн зургийн URL
    @Column(length = 512)
    private String avatarUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
