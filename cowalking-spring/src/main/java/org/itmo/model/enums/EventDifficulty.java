// src/main/java/org/itmo/model/enums/EventDifficulty.java
package org.itmo.model.enums;

public enum EventDifficulty {
    EASY("Легкая"),
    MEDIUM("Средняя"),
    HARD("Сложная");

    private final String displayName;

    EventDifficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}