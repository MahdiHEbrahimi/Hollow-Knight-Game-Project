package com.mahdi.model.characters;

import com.mahdi.model.characters.enemies.*;

public enum EnemyType {

    CRAWLED("crawled") {
        @Override
        public Enemy getInstance(float x, float y, Player player) {
            return new Crawled(x, y, player, false);
        }
    },

    CRYSTAL_CRAWLER("CrystalCrawler") {
        @Override
        public Enemy getInstance(float x, float y, Player player) {
            return new Crawled(x, y, player, true);
        }
    },

    MOSQUITO("mosquito") {
        @Override
        public Enemy getInstance(float x, float y, Player player) {
            return new Mosquito(x, y, player);
        }
    },

    HORNHEAD_HUSK("HornheadHusk") {
        @Override
        public Enemy getInstance(float x, float y, Player player) {
            return new HornheadHusk(x, y, player);
        }
    },

    CRYSTAL_GUARDAIN ("CrystalGuardian"){
        @Override
        public Enemy getInstance(float x, float y, Player player) {
            return new CrystalGuardian(x, y, player);
        }
    },

    FALSE_KNIGHT ("false knight"){
        @Override
        public Enemy getInstance(float x, float y, Player player) {
            return new FalseKnight(x, y, player, 40);
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
