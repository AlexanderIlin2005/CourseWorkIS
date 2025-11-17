
package org.itmo.model.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Converter
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {

    
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC");

    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        
        
        
        
        
        
        return Timestamp.valueOf(zonedDateTime.withZoneSameInstant(DEFAULT_ZONE_ID).toLocalDateTime());
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        
        return timestamp.toLocalDateTime().atZone(DEFAULT_ZONE_ID);
    }
}