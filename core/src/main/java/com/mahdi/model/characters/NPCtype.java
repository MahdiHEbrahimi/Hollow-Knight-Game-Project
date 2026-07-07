package com.mahdi.model.characters;

import com.mahdi.model.characters.enemies.*;
import com.mahdi.model.characters.npc.Zote;
public enum NPCtype {
    ZOTE("zote") {
        @Override
        public BaseCharacter getInstance(float x, float y, Player player) {
            return new Zote(x, y, player);
        }
    };

    private final String name;

    NPCtype(String name) {
        this.name = name;
    }

    public abstract BaseCharacter getInstance(float x, float y, Player player);

    public String getName() {
        return name;
    }
}
