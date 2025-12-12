package org.itmo.repository;

import org.itmo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone); // Added method to find by phone

    // --- ДОБАВЛЕНО: МЕТОДЫ ПОИСКА ПО ID СОЦСЕТЕЙ ---
    Optional<User> findByTelegramId(String telegramId);
    Optional<User> findByVkId(String vkId);
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---
}
