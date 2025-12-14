// src/main/java/org/itmo/mapper/ReviewMapper.java
package org.itmo.mapper;

import org.itmo.dto.ReviewDto;
import org.itmo.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "id", ignore = true) // ID генерируется БД
    @Mapping(target = "event", ignore = true) // Устанавливается вручную в сервисе
    @Mapping(target = "user", ignore = true) // Устанавливается вручную в сервисе
    @Mapping(target = "createdAt", ignore = true) // Устанавливается вручную или по умолчанию в сущности
    Review toReview(ReviewDto reviewDto);
}