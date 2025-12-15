package org.itmo.service;

import org.itmo.model.User;
import org.itmo.model.Event;
import org.itmo.model.enums.UserRole;
import org.itmo.repository.UserRepository;
import org.itmo.repository.EventRepository; 
import org.itmo.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

import org.itmo.model.enums.ParticipationStatus;
import org.itmo.model.Participation;

import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final EventRepository eventRepository; 

    private final ParticipationRepository participationRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class); 

    @Autowired
    private PasswordEncoder passwordEncoder;

    
    @Transactional
    public User save(User user) {
        logger.info("Saving user: ID={}, Username={}", user.getId(), user.getUsername()); 
        if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().startsWith("$2a$")) {
            logger.debug("Hashing new password for user: {}", user.getUsername()); 
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            logger.debug("Password for user {} is already hashed or empty, skipping hashing.", user.getUsername()); 
        }
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        logger.info("User saved successfully with ID: {}", saved.getId());
        return saved;
    }

    public Optional<User> findById(Long id) {
        logger.debug("Looking up user by ID: {}", id); 
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        logger.debug("Looking up user by username: {}", username); 
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        logger.debug("Looking up user by email: {}", email); 
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByPhone(String phone) {
        logger.debug("Looking up user by phone: {}", phone); 
        return userRepository.findByPhone(phone);
    }

    
    public Optional<User> findByTelegramId(String telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    public Optional<User> findByVkId(String vkId) {
        return userRepository.findByVkId(vkId);
    }
    

    public String encodePassword(String rawPassword) {
        logger.debug("Encoding password: {}", rawPassword); 
        return passwordEncoder.encode(rawPassword);
    }

    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user details by username: {}", username); 
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found during authentication: {}", username); 
                    return new UsernameNotFoundException("User not found: " + username);
                });
        logger.info("Loaded user details: ID={}, Username={}, Role={}", user.getId(), user.getUsername(), user.getRole()); 
        return user;
    }

    
    public List<Event> findOrganizedEvents(Long userId) {
        
        
        return eventRepository.findByOrganizerId(userId);
    }

    
    public List<Participation> findPendingApplicationsSentByUser(Long userId) {
        return participationRepository.findByParticipantIdAndStatus(userId, ParticipationStatus.PENDING);
    }

    public List<Participation> findPendingApplicationsForOrganizer(Long organizerId) {
        return participationRepository.findByEventOrganizerIdAndStatus(organizerId, ParticipationStatus.PENDING);
    }

    
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void freezeUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void unfreezeUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void changeUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole);
        userRepository.save(user);
    }
    

    
    public List<Event> findConfirmedEventsForParticipant(Long participantId) {
        List<Participation> participations = participationRepository.findByParticipantIdAndStatus(
                participantId,
                ParticipationStatus.CONFIRMED
        );
        return participations.stream()
                .map(Participation::getEvent)
                .collect(Collectors.toList());
    }
    

}
