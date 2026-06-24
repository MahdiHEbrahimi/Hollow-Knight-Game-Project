package com.mahdi.model.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mahdi.model.status.AppStatus;

public abstract class Enemy extends BaseCharacter {

    protected Enemy(float x, float y, float width, float height, float maxXSpeed, float acceleration, int hp) {
        super(x, y, width, height, maxXSpeed, acceleration, hp);
    }

    @Override
    public void die() {
        // todo
        this.isAlive = false;
        this.hasGravity = true;
        this.velocity.x = 0;
        this.isMoving = false;
    }

    public abstract Corpse getCorpse();

    public void takeDamage(int damage) {
        if (!isAlive) return;
        hp -= damage;
        if (hp <= 0) {
            AppStatus.getGameEngine().enemyIsDead(this);
        }
    }

}
