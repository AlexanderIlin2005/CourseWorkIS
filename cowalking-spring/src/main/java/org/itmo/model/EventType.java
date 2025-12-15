
package org.itmo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cowalking_event_types")
@Getter
@Setter
@NoArgsConstructor
public class EventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; 

    @Column(length = 1000)
    private String description; 

    public EventType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}