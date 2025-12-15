
package org.itmo.controller;

import org.itmo.dto.UserDto;
import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
import org.itmo.service.EventService; 
import org.itmo.service.FileStorageService;
import org.itmo.mapper.EventMapper; 
import org.itmo.mapper.UserMapper; 
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; 
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List; 
import java.util.stream.Collectors; 

import org.itmo.dto.EventDto;
import org.itmo.dto.ParticipationDto;
import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus;

import org.itmo.model.Participation;

import org.itmo.mapper.ParticipationMapper;
import org.itmo.repository.EventRepository;

import java.util.ArrayList;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.RequestParam; 

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper; 

    private final FileStorageService fileStorageService; 

    
    @Autowired
    private EventService eventService; 

    @Autowired
    private EventMapper eventMapper; 

    
    @Autowired
    private ParticipationMapper participationMapper; 
    

    @Autowired
    private EventRepository eventRepository;


    

    private static final Logger logger = LoggerFactory.getLogger(UserController.class); 

    @GetMapping("/users/profile")
    public String profile(Model model) {
        logger.info("Handling GET request for /users/profile");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Current authentication: {}", auth);

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            logger.warn("User not authenticated or principal is not of type User, redirecting to /login");
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();
        logger.info("Current user: ID={}, Username={}", currentUser.getId(), currentUser.getUsername());

        
        UserDto userDto = userMapper.toUserDto(currentUser);
        model.addAttribute("user", userDto);

        
        
        List<Event> organizedEvents = new ArrayList<>();
        List<Event> allActiveEvents = eventRepository.findEventsWithAllDetailsByStatus(EventStatus.ACTIVE);
        List<Event> allCompletedEvents = eventRepository.findEventsWithAllDetailsByStatus(EventStatus.COMPLETED);

        
        for (Event event : allActiveEvents) {
            if (event.getOrganizer().getId().equals(currentUser.getId())) {
                organizedEvents.add(event);
            }
        }
        for (Event event : allCompletedEvents) {
            if (event.getOrganizer().getId().equals(currentUser.getId())) {
                organizedEvents.add(event);
            }
        }
        

        
        List<EventDto> activeOrganizedEvents = allActiveEvents.stream()
                .filter(event -> event.getOrganizer().getId().equals(currentUser.getId()))
                .map(event -> {
                    eventService.updateEventStatusIfNeeded(event);
                    return event;
                })
                .filter(event -> EventStatus.ACTIVE.equals(event.getStatus()))
                .map(eventMapper::toEventDto)
                .collect(Collectors.toList());

        List<EventDto> completedOrganizedEvents = allCompletedEvents.stream()
                .filter(event -> event.getOrganizer().getId().equals(currentUser.getId()))
                .map(event -> {
                    eventService.updateEventStatusIfNeeded(event);
                    return event;
                })
                .filter(event -> EventStatus.COMPLETED.equals(event.getStatus()))
                .map(eventMapper::toEventDto)
                .collect(Collectors.toList());

        
        
        List<Participation> sentApplications = userService.findPendingApplicationsSentByUser(currentUser.getId());
        List<ParticipationDto> sentApplicationDtos = sentApplications.stream()
                .map(ParticipationDto::new) 
                .collect(Collectors.toList());
        model.addAttribute("sentApplications", sentApplicationDtos);

        
        List<Participation> receivedApplications = userService.findPendingApplicationsForOrganizer(currentUser.getId());
        List<ParticipationDto> receivedApplicationDtos = receivedApplications.stream()
                .map(ParticipationDto::new) 
                .collect(Collectors.toList());
        model.addAttribute("receivedApplications", receivedApplicationDtos);
        

        
        List<Event> confirmedEvents = userService.findConfirmedEventsForParticipant(currentUser.getId());
        List<EventDto> confirmedEventDtos = confirmedEvents.stream()
                .map(eventMapper::toEventDto)
                .collect(Collectors.toList());
        model.addAttribute("confirmedEvents", confirmedEventDtos);
        

        model.addAttribute("activeOrganizedEvents", activeOrganizedEvents);
        model.addAttribute("completedOrganizedEvents", completedOrganizedEvents);

        return "profile";
    }

    @PostMapping("/users/update")
    public String updateProfile(
            @RequestParam("id") Long id,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "telegramId", required = false) String telegramId,
            @RequestParam(value = "vkId", required = false) String vkId,
            @RequestParam(value = "photo", required = false) MultipartFile photoFile,
            Model model) {

        logger.info("Handling POST request for /users/update with parameters: id={}, email={}", id, email);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Current authentication: {}", auth);

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            logger.warn("User not authenticated or principal is not of type User, redirecting to /login");
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();
        logger.info("Current user attempting update: ID={}, Username={}", currentUser.getId(), currentUser.getUsername());

        
        if (!currentUser.getId().equals(id) && !currentUser.getRole().equals(UserRole.ADMIN)) {
            logger.warn("User {} (ID={}) attempted to update profile for user ID {}, access denied.", currentUser.getUsername(), currentUser.getId(), id);
            return "redirect:/users/profile";
        }

        try {
            
            User userToUpdate = userService.findById(id)
                    .orElseThrow(() -> {
                        logger.error("User to update (ID={}) not found in database.", id);
                        return new RuntimeException("User not found");
                    });
            logger.info("Found user in DB to update: ID={}, Username={}", userToUpdate.getId(), userToUpdate.getUsername());

            
            userToUpdate.setEmail(email);
            userToUpdate.setPhone(phone);
            userToUpdate.setBio(bio);
            userToUpdate.setTelegramId(telegramId);
            userToUpdate.setVkId(vkId);

            logger.info("Updated fields for user {}: Email={}, Phone={}, Bio={}", userToUpdate.getId(), userToUpdate.getEmail(), userToUpdate.getPhone(), userToUpdate.getBio());

            
            if (photoFile != null && !photoFile.isEmpty()) {
                try {
                    String photoPath = fileStorageService.storeFile(photoFile, "user");
                    
                    userToUpdate.setPhotoUrl("/" + photoPath);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store user photo", e);
                }
            }

            User updatedUser = userService.save(userToUpdate);
            logger.info("User profile updated successfully: ID={}", updatedUser.getId());

            
            UserDetails freshUserDetails = userService.loadUserByUsername(updatedUser.getUsername());
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    freshUserDetails,
                    auth.getCredentials(),
                    freshUserDetails.getAuthorities()
            );
            newAuth.setDetails(auth.getDetails());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            return "redirect:/users/profile?success=true";

        } catch (Exception e) {
            logger.error("Error updating user profile (ID={}): {}", id, e.getMessage(), e);
            model.addAttribute("error", "Update failed: " + e.getMessage());

            
            UserDto errorDto = new UserDto();
            errorDto.setId(id);
            errorDto.setEmail(email);
            errorDto.setPhone(phone);
            errorDto.setBio(bio);
            errorDto.setTelegramId(telegramId);
            errorDto.setVkId(vkId);
            model.addAttribute("user", errorDto);

            
            try {
                User userForEvents = userService.findById(id).orElse(null);
                if (userForEvents != null) {
                    List<org.itmo.dto.EventDto> organizedEventDtos = userService.findOrganizedEvents(userForEvents.getId()).stream()
                            .map(eventMapper::toEventDto)
                            .collect(Collectors.toList());
                    model.addAttribute("organizedEvents", organizedEventDtos);
                } else {
                    model.addAttribute("organizedEvents", List.of());
                }
            } catch (Exception e2) {
                logger.error("Error fetching events organized by user ID={} for error page", id, e2);
                model.addAttribute("organizedEvents", List.of());
            }

            return "profile";
        }
    }

    @GetMapping("/registration")
    public String registrationForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUser(@ModelAttribute UserDto userDto, Model model) {
        try {
            
            if (userDto.getPhone() != null && !userDto.getPhone().isEmpty() && !userDto.getPhone().matches("^\\+7\\(\\d{3}\\)-\\d{3}-\\d{2}-\\d{2}$")) {
                model.addAttribute("error", "Invalid phone number format. Use +7(xxx)-xxx-xx-xx.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }

            
            if (userService.findByUsername(userDto.getUsername()).isPresent()) {
                model.addAttribute("error", "Username already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }

            
            if (userService.findByEmail(userDto.getEmail()).isPresent()) {
                model.addAttribute("error", "Email already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }

            
            if (userDto.getPhone() != null && !userDto.getPhone().isEmpty() && userService.findByPhone(userDto.getPhone()).isPresent()) {
                model.addAttribute("error", "Phone number already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }

            
            
            if (userDto.getTelegramId() != null && !userDto.getTelegramId().isEmpty() && userService.findByTelegramId(userDto.getTelegramId()).isPresent()) {
                model.addAttribute("error", "Telegram ID already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }

            
            if (userDto.getVkId() != null && !userDto.getVkId().isEmpty() && userService.findByVkId(userDto.getVkId()).isPresent()) {
                model.addAttribute("error", "VK ID already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }
            

            
            User user = new User(); 
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setPhone(userDto.getPhone());
            user.setBio(userDto.getBio());
            
            user.setTelegramId(userDto.getTelegramId());
            user.setVkId(userDto.getVkId());
            
            
            user.setPassword(userService.encodePassword(userDto.getPassword()));
            
            user.setRole(UserRole.PARTICIPANT); 
            user.setActive(true);

            User savedUser = userService.save(user);
            model.addAttribute("message", "Registration successful! Please log in.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("userDto", userDto);
            return "registration";
        }
    }
}