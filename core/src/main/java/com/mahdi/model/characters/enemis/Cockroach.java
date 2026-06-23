package com.mahdi.model.characters.enemis;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mahdi.model.characters.Enemy;
import com.mahdi.model.characters.Player;

public class Cockroach extends Enemy {
    private int hp = 3;
    private final ShapeRenderer debugRenderer;
    private final Player player;   // ارجاع به بازیکن (برای AI آینده)

    public Cockroach(float x, float y, Player player) {
        super(x, y, 80, 120, 0f, 0f); // عرض ۸۰، ارتفاع ۱۲۰
        this.player = player;
        this.debugRenderer = new ShapeRenderer();
    }

    @Override
    public void updateCustomLogic(float delta) {
        // فعلاً هوش مصنوعی ندارد – فقط یک مستطیل زرد است.
    }

    @Override
    public void draw(Batch batch) {
        batch.end();
        debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(Color.YELLOW);
        debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        debugRenderer.end();
        batch.begin();
    }

    public void takeDamage(int damage) {
        if (!isAlive) return;
        hp -= damage;
        System.out.println("Cockroach damaged! HP = " + hp);
        if (hp <= 0) {
            die();
        }
    }

    public int getHp() {
        return hp;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
    }
}
