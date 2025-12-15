
package org.itmo.config;

import org.itmo.model.Location;
import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.LocationService;
import org.itmo.service.UserService;
import org.itmo.repository.LocationRepository; 
import org.itmo.repository.EventTypeRepository;
import org.itmo.model.EventType; 

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct; 
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserService userService;
    private final LocationService locationService;
    private final LocationRepository locationRepository; 
    private final EventTypeRepository eventTypeRepository; 

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @PostConstruct 
    public void initializeData() {
        logger.info("Running DataInitializer");

        
        initializeLocations();

        
        initializeAdminUser();

        initializeEventTypes(); 
    }

    private void initializeLocations() {
        if (locationService.findAll().isEmpty()) {
            logger.info("No locations found. Creating default locations.");

            List<Location> defaultLocations = Arrays.asList(
                    createLocation("Центральный парк культуры и отдыха (ЦПКиО)", "наб. реки Фонтанки, 121, Санкт-Петербург", 59.9343, 30.3351, "Большой парк с прудами и зеленью в центре города."),
                    createLocation("Парк 300-летия Санкт-Петербурга", "Большой проспект П.С., 100, Санкт-Петербург", 59.9456, 30.2987, "Современный парк с красивыми аллеями."),
                    createLocation("Невский проспект", "Невский проспект, Санкт-Петербург", 59.9343, 30.3178, "Популярное место для прогулок в центре.")
            );

            
            locationRepository.saveAll(defaultLocations); 
            logger.info("Default locations created successfully.");
        } else {
            logger.info("Locations already exist, skipping creation.");
        }
    }

    private Location createLocation(String name, String address, double lat, double lon, String description) {
        Location location = new Location();
        location.setName(name);
        location.setAddress(address);
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setDescription(description);
        return location;
    }

    private void initializeAdminUser() {
        User existingAdmin = userService.findByUsername("admin").orElse(null);
        if (existingAdmin == null) {
            logger.info("Admin user not found. Creating default admin user.");

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@cowalking.com");
            
            admin.setPassword("admin"); 
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);

            userService.save(admin);
            logger.info("Default admin user created successfully.");
        } else {
            logger.info("Admin user already exists. Updating password and role.");
            
            
            
            
            
            userService.save(existingAdmin);
            logger.info("Default admin user updated.");
            
        }
    }

    private void initializeEventTypes() {
        if (eventTypeRepository.count() == 0) {
            logger.info("No event types found. Creating default event types.");
            List<EventType> defaultTypes = Arrays.asList(
                    new EventType("Пешая прогулка", "Неспешные прогулки по городу"),
                    new EventType("Бег", "Беговые тренировки в парках"),
                    new EventType("Скандинавская ходьба", "Прогулки со специальными палками"),
                    new EventType("Велопрогулка", "Веломаршруты по городу и паркам"),
                    new EventType("Прогулка на роликах", "Роликовые маршруты для активного отдыха")
            );
            eventTypeRepository.saveAll(defaultTypes);
            logger.info("Default event types created successfully.");
        } else {
            logger.info("Event types already exist, skipping creation.");
        }
    }

}