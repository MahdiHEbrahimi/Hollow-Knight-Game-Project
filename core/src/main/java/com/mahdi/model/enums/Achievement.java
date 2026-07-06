package com.mahdi.model.enums;

public enum Achievement {
    CHEATER(
        "Cheater",
        "Use a cheat code."
    ),
    COMPLETION(
        "Completion",
        "Finish the game."
    ),
    FALSE_KNIGHT(
        "False Knight",
        "Defeat the False Knight boss."
    ),
    HUNTER(
        "True Hunter",
        "Kill every enemy type in the game."
    ),
    SPEEDRUN(
        "Speedrun",
        "Finish the game within the time limit."
    );

    private final String displayName;
    private final String description;
    private boolean active;

    Achievement(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
        this.active = false; // در ابتدا همه قفل هستند
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
