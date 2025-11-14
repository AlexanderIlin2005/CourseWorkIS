// src/main/java/org/itmo/model/converters/ZonedDateTimeConverter.java
package org.itmo.model.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Converter
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {

    // Установите вашу часовую зону по умолчанию, если не используется UTC
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC");

    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        // Преобразуем ZonedDateTime в LocalDateTime в целевой зоне (например, UTC) и затем в Timestamp
        // Или сохраняйте как есть, если ваша БД и драйвер поддерживают полноценный ZonedDateTime.
        // Часто сохраняют как LocalDateTime (TIMESTAMP WITHOUT TIME ZONE) в UTC и восстанавливают с предполагаемой зоны.
        // Для простоты и избежания потенциальных проблем с ZonedDateTime в EclipseLink, можно сохранять как LocalDateTime.
        // Но если ZonedDateTime критичен, возможно, нужно будет использовать VARCHAR или BIGINT (millis).
        // Пока используем упрощенный вариант - сохраняем как LocalDateTime в UTC.
        return Timestamp.valueOf(zonedDateTime.withZoneSameInstant(DEFAULT_ZONE_ID).toLocalDateTime());
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        // Восстанавливаем как ZonedDateTime в UTC, или используйте вашу часовую зону по умолчанию
        return timestamp.toLocalDateTime().atZone(DEFAULT_ZONE_ID);
    }
}