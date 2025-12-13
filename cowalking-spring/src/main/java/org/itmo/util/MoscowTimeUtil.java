// src/main/java/org/itmo/util/MoscowTimeUtil.java
package org.itmo.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class MoscowTimeUtil {

    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");

    /**
     * Получает текущее время в Москве как LocalDateTime.
     */
    public static LocalDateTime getCurrentMoscowTime() {
        return LocalDateTime.now(MOSCOW_ZONE);
    }

    /**
     * Конвертирует LocalDateTime (предполагается, что в UTC или без зоны)
     * в LocalDateTime по Московскому времени.
     * В нашем случае, так как в БД хранится LocalDateTime без зоны,
     * мы будем считать его "московским" и просто сравнивать.
     * Для корректного сравнения с текущим временем Москвы,
     * нужно убедиться, что оба объекта относятся к одной зоне.
     */
    public static LocalDateTime toMoscowTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        // Если localDateTime из БД, мы считаем его московским.
        // Для сравнения с текущим временем Москвы, конвертируем текущее в UTC и сравниваем?
        // Но проще: конвертируем текущее время Москвы в LocalDateTime без зоны, как в БД.
        // На самом деле, если в БД хранится LocalDateTime, и мы его считаем московским,
        // то для сравнения с текущим московским временем, нужно получить текущее время Москвы как LocalDateTime.
        // Это и делает getCurrentMoscowTime().
        return localDateTime; // Предполагаем, что localDateTime уже в московской зоне
    }

    /**
     * Проверяет, завершено ли событие.
     * Событие считается завершённым, если его endTime <= текущего московского времени.
     */
    public static boolean isEventCompleted(LocalDateTime eventEndTime) {
        if (eventEndTime == null) {
            return false; // Событие без времени окончания считается активным
        }
        LocalDateTime currentMoscowTime = getCurrentMoscowTime();
        return !eventEndTime.isAfter(currentMoscowTime);
    }
}