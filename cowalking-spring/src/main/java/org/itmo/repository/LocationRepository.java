package org.itmo.repository;

import org.itmo.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    // Можно добавить кастомные методы, если нужно
}
