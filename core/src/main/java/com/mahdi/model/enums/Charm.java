package com.mahdi.model.enums;

public enum Charm {
    DASHMASTER(
        "Dashmaster",
        "Reduces Dash cooldown, allowing more frequent dashing.",
        false
    ),
    HEAVY_BLOW(
        "HeavyBlow",
        "Increases knockback force on enemies.",
        false
    ),
    QUICK_FOCUS(
        "Quick Focus",
        "Increases the speed of Focus healing.",
        false
    ),
    QUICK_SLASH(
        "Quick Slash",
        "Increases attack speed with your Nail.",
        false
    ),
    SHARP_SHADOW(
        "Sharp Shadow",
        "Deals damage to enemies when dashing through them.",
        false
    ),
    SOUL_CATCHER(
        "Soul Catcher",
        "Increases the amount of Soul gained from striking enemies.",
        false
    ),
    UNBREAKABLE(
        "Unbreakable",
        "Grants immunity to knockback and increased durability.",
        false
    ),
    VOID_HEART(
        "Void Heart",
        "Unlocks hidden potential within the bearer.",
        false
    );

    private final String displayName;
    private final String description;
    private boolean active;

    Charm(String displayName, String description, boolean activeDefault) {
        this.displayName = displayName;
        this.description = description;
        this.active = activeDefault;
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
        System.out.println(this.getDisplayName() + active);
        this.active = active;
    }

    public void toggle() {
        active = !active;
    }
}
