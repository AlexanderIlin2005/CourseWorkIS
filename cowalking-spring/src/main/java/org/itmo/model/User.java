
package org.itmo.model;

import org.itmo.model.converters.LocalDateTimeConverter; 
import org.itmo.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "cowalking_users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = true) 
    private String phone;

    @Column(length = 1000, nullable = true) 
    private String bio;

    
    @Column(unique = true, nullable = true) 
    private String telegramId;

    @Column(unique = true, nullable = true) 
    private String vkId;
    

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean active = true;

    @Convert(converter = LocalDateTimeConverter.class) 
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Convert(converter = LocalDateTimeConverter.class) 
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    
    @Column(length = 500)
    private String photoUrl;
    

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    
    public boolean isOrganizerOf(Event event) {
        if (event == null || event.getOrganizer() == null) {
            return false;
        }
        return this.getId().equals(event.getOrganizer().getId());
    }
}