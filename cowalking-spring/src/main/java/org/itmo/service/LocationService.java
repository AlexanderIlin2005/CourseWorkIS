package org.itmo.service;

import org.itmo.model.Location;
import org.itmo.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    public Location save(Location location) {
        return locationRepository.save(location);
    }

    public void saveAll(List<Location> locations) {
        locationRepository.saveAll(locations);
    }

    public void deleteById(Long id) {
        locationRepository.deleteById(id);
    }
}
