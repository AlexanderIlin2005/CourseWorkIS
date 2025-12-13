package org.itmo.service;

import org.itmo.model.User;
import org.itmo.model.Event;
import org.itmo.model.enums.UserRole;
import org.itmo.repository.UserRepository;
import org.itmo.repository.EventRepository; // Импортируем EventRepository
import org.itmo.repository.ParticipationRepository;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // <-- Добавьте импорт

import org.itmo.model.enums.ParticipationStatus;
import org.itmo.model.Participation;



@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final EventRepository eventRepository; // <-- Внедряем EventRepository

    private final ParticipationRepository participationRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class); // <-- Добавьте логгер

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- ИСПРАВЛЕНО: Метод save теперь транзакционен и корректно обрабатывает пароль ---
    @Transactional
    public User save(User user) {
        logger.info("Saving user: ID={}, Username={}", user.getId(), user.getUsername()); // <-- Логируем сохранение
        if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().startsWith("$2a$")) {
            logger.debug("Hashing new password for user: {}", user.getUsername()); // <-- Логируем хеширование
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            logger.debug("Password for user {} is already hashed or empty, skipping hashing.", user.getUsername()); // <-- Логируем пропуск хеширования
        }
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        logger.info("User saved successfully with ID: {}", saved.getId());
        return saved;
    }

    public Optional<User> findById(Long id) {
        logger.debug("Looking up user by ID: {}", id); // <-- Логируем поиск
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        logger.debug("Looking up user by username: {}", username); // <-- Логируем поиск
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        logger.debug("Looking up user by email: {}", email); // <-- Логируем поиск
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByPhone(String phone) {
        logger.debug("Looking up user by phone: {}", phone); // <-- Логируем поиск
        return userRepository.findByPhone(phone);
    }

    // --- ДОБАВЛЕНО: МЕТОДЫ ПОИСКА ПО ID СОЦСЕТЕЙ ---
    public Optional<User> findByTelegramId(String telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    public Optional<User> findByVkId(String vkId) {
        return userRepository.findByVkId(vkId);
    }
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    public String encodePassword(String rawPassword) {
        logger.debug("Encoding password: {}", rawPassword); // <-- Логируем вызов encodePassword
        return passwordEncoder.encode(rawPassword);
    }

    // --- ДОБАВЛЕНО: Метод findAll для получения всех пользователей ---
    public List<User> findAll() {
        return userRepository.findAll();
    }
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user details by username: {}", username); // <-- Логируем загрузку
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found during authentication: {}", username); // <-- Логируем отсутствие
                    return new UsernameNotFoundException("User not found: " + username);
                });
        logger.info("Loaded user details: ID={}, Username={}, Role={}", user.getId(), user.getUsername(), user.getRole()); // <-- Логируем найденного
        return user;
    }

    // Добавим метод для получения событий, организованных пользователем
    public List<Event> findOrganizedEvents(Long userId) {
        // Используем EventRepository для поиска
        // Предположим, в EventRepository есть метод findByOrganizerId
        return eventRepository.findByOrganizerId(userId);
    }

    // Метод уже есть в ParticipationService, но можно сделать фасад
    public List<Participation> findPendingApplicationsSentByUser(Long userId) {
        return participationRepository.findByParticipantIdAndStatus(userId, ParticipationStatus.PENDING);
    }

    public List<Participation> findPendingApplicationsForOrganizer(Long organizerId) {
        return participationRepository.findByEventOrganizerIdAndStatus(organizerId, ParticipationStatus.PENDING);
    }
}
