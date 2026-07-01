package com.mahdi.model.characters;

import com.mahdi.model.characters.enemies.Crawled;
import com.mahdi.model.characters.enemies.Mosquito;

public enum EnemyType {

    CRAWLED("crawled") {
        @Override
        public Enemy getInstance(float x, float y, Player player) {
            return new Crawled(x, y, player);
        }
    },

    MOSQUITO("mosquito") {
        @Override
        public Enemy getInstance(float x, float y, Player player) {
            return new Mosquito(x, y, player);
        }
    };

    private final String name;

    EnemyType(String name) {
        this.name = name;
    }

    public abstract Enemy getInstance(float x, float y, Player player);

    public String getName() {
        return name;
    }
}
