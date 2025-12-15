package org.itmo.model;

import org.itmo.model.converters.LocalDateTimeConverter; 
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; 

@Entity
@Table(name = "cowalking_locations")
@Getter
@Setter
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(length = 1000)
    private String description;

    
    @Convert(converter = LocalDateTimeConverter.class) 
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Convert(converter = LocalDateTimeConverter.class) 
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
}
