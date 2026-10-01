package com.ccpm.users.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    /** Se guarda el hash BCrypt, nunca la contraseña en texto plano. */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 1000)
    private String token;

    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @Column(nullable = false)
    private LocalDateTime modified;

    @Column(name = "last_login", nullable = false)
    private LocalDateTime lastLogin;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Phone> phones = new ArrayList<>();

    /** Mantiene sincronizados los dos lados de la relación. */
    public void addPhone(Phone phone) {
        phones.add(phone);
        phone.setUser(this);
    }

    @PreUpdate
    void onUpdate() {
        this.modified = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    public LocalDateTime getModified() { return modified; }
    public void setModified(LocalDateTime modified) { this.modified = modified; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Phone> getPhones() { return phones; }
}
