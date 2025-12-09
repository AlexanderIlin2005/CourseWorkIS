package org.itmo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LocationDto {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
