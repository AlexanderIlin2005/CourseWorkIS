// src/main/java/org/itmo/config/LocationDataInitializer.java
package org.itmo.config;

import org.itmo.model.Location;
import org.itmo.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationDataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final LocationService locationService;

    private static final Logger logger = LoggerFactory.getLogger(LocationDataInitializer.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Running LocationDataInitializer");
        initializeLocations();
    }

    private void initializeLocations() {
        // Пример добавления локаций Петербурга
        if (locationService.findAll().isEmpty()) { // Проверяем, есть ли уже локации
            logger.info("No locations found. Creating default locations.");

            Location location1 = new Location();
            location1.setName("Центральный парк культуры и отдыха (ЦПКиО)");
            location1.setAddress("наб. реки Фонтанки, 121, Санкт-Петербург");
            location1.setLatitude(59.9343); // Пример координат
            location1.setLongitude(30.3351);
            location1.setDescription("Большой парк с прудами и зеленью в центре города.");
            locationService.save(location1);

            Location location2 = new Location();
            location2.setName("Парк 300-летия Санкт-Петербурга");
            location2.setAddress("Большой проспект П.С., 100, Санкт-Петербург");
            location2.setLatitude(59.9456);
            location2.setLongitude(30.2987);
            location2.setDescription("Современный парк с красивыми аллеями.");
            locationService.save(location2);

            Location location3 = new Location();
            location3.setName("Невский проспект");
            location3.setAddress("Невский проспект, Санкт-Петербург");
            location3.setLatitude(59.9343);
            location3.setLongitude(30.3178);
            location3.setDescription("Популярное место для прогулок в центре.");
            locationService.save(location3);

            logger.info("Default locations created successfully.");
        } else {
            logger.info("Locations already exist, skipping creation.");
        }
    }
}