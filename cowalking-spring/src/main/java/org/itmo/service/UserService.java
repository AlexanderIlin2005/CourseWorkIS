package org.itmo.service;

import org.itmo.model.User;
import org.itmo.model.Event;
import org.itmo.model.enums.UserRole;
import org.itmo.repository.UserRepository;
import org.itmo.repository.EventRepository; // Импортируем EventRepository
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Импортируем Transactional

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final EventRepository eventRepository; // <-- Внедряем EventRepository

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- ИСПРАВЛЕНО: Метод save теперь транзакционен и корректно обрабатывает пароль ---
    @Transactional
    public User save(User user) {
        // Хешируем пароль ТОЛЬКО если он был *изменен* (не пустой и не начинается с $2a$)
        // Предполагаем, что все BCrypt хеши начинаются с $2a$, $2b$, $2y$
        if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // Роли и активность, скорее всего, не должны изменяться через updateProfile
        // createdAt не изменяется при update
        // updatedAt обновляется автоматически при save, если у вас есть аннотация @UpdateTimestamp или вы делаете это вручную
        // Вручную обновим updatedAt
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // --- ДОБАВЛЕНО: Метод findAll для получения всех пользователей ---
    public List<User> findAll() {
        return userRepository.findAll();
    }
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return user;
    }

    // Добавим метод для получения событий, организованных пользователем
    public List<Event> findOrganizedEvents(Long userId) {
        // Используем EventRepository для поиска
        // Предположим, в EventRepository есть метод findByOrganizerId
        return eventRepository.findByOrganizerId(userId);
    }
}
