package com.mahdi.model.enums;

public enum Charm {
    DASHMASTER(
        "Dashmaster",
        "Reduces Dash cooldown, allowing more frequent dashing.",
        false
    ),
    HEAVY_BLOW(
        "Heavy Blow",
        "Greatly increases knockback force, pushing enemies farther away.",
        false
    ),
    QUICK_FOCUS(
        "Quick Focus",
        "Increases Focus speed, reducing time needed to heal.",
        false
    ),
    QUICK_SLASH(
        "Quick Slash",
        "Greatly increases Nail attack speed.",
        false
    ),
    SHARP_SHADOW(
        "Sharp Shadow",
        "Dash through enemies dealing damage. Increases Dash length by 20%.",
        false
    ),
    SOUL_CATCHER(
        "Soul Catcher",
        "Increases Soul gained from striking enemies with the Nail.",
        false
    ),
    UNBREAKABLE(
        "Unbreakable",
        "Strengthens the Knight, increasing Nail damage.",
        false
    ),
    VOID_HEART(
        "Void Heart",
        "Spell damage +50%, unlocks dark spells.",
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
        this.active = active;
    }

    public void toggle() {
        active = !active;
    }
}
