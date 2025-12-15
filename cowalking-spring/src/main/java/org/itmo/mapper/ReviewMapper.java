
package org.itmo.mapper;

import org.itmo.dto.ReviewDto;
import org.itmo.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "id", ignore = true) 
    @Mapping(target = "event", ignore = true) 
    @Mapping(target = "user", ignore = true) 
    @Mapping(target = "createdAt", ignore = true) 
    Review toReview(ReviewDto reviewDto);
}