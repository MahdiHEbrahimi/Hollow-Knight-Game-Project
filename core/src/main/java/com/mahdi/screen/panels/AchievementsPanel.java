package com.mahdi.screen.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.mahdi.model.enums.Achievement;
import com.mahdi.model.enums.MenuType;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.ui.MenuButton;

public class AchievementsPanel extends BasePanel {

    private final BitmapFont font;
    private TextureAtlas atlas;
    private TextureRegion lockRegion;

    public AchievementsPanel() {
        this.font = FontManager.getInstance().getEnglishMenuFont();
        atlas = new TextureAtlas("Achievements/Achievements.atlas");
        lockRegion = atlas.findRegion("Lock");

        setFillParent(true);
        align(Align.top);
        padTop(60f);

        // عنوان
        Label title = new Label("ACHIEVEMENTS", new Label.LabelStyle(font, new Color(font.getColor())));
        title.setFontScale(2.2f);
        add(title).padBottom(20f).row();

        // جدول دستاوردها
        Table achievementsTable = new Table();
        achievementsTable.align(Align.center);
        achievementsTable.defaults().space(15f).center();

        for (Achievement achievement : Achievement.values()) {
            String regionName = achievement.name().toLowerCase();
            TextureRegion region = atlas.findRegion(regionName);
            if (region == null) continue;

            boolean unlocked = achievement.isActive();

            Image img = new Image(region);
            img.setAlign(Align.center);
            img.setColor(unlocked ? Color.WHITE : new Color(0.35f, 0.35f, 0.35f, 0.65f));

            Stack imageStack = new Stack();
            imageStack.add(img);
            if (!unlocked && lockRegion != null) {
                Image lockImg = new Image(lockRegion);
                lockImg.setAlign(Align.bottomRight);
                lockImg.setColor(Color.WHITE);
                imageStack.add(lockImg);
            }

            // ☀️ هر لیبل با رنگ مستقل خودش ساخته شود
            Label nameLabel = new Label(achievement.getDisplayName(),
                new Label.LabelStyle(font, new Color(font.getColor())));
            nameLabel.setFontScale(1.2f);
            nameLabel.setAlignment(Align.center);
            Label descLabel = new Label(achievement.getDescription(),
                new Label.LabelStyle(font, new Color(font.getColor())));
            descLabel.setFontScale(0.8f);
            descLabel.setAlignment(Align.center);

            Table textTable = new Table();
            textTable.align(Align.center);
            textTable.add(nameLabel).center().row();
            textTable.add(descLabel).center().row();

            Table row = new Table();
            row.align(Align.center);
            row.add(imageStack).size(180, 180).center();
            row.add(textTable).padLeft(15f).center();

            achievementsTable.add(row).center().padBottom(10f).row();
        }

        add(achievementsTable).expand().center().padBottom(15f).row();

        // دکمه بازگشت
        MenuButton backButton = new MenuButton("BACK", font, () -> {
            AppStatus.ChangeMenuAndPanel(MenuType.MAIN_MENU);
        });
        add(backButton).padBottom(25f).row();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.setColor(Color.WHITE);   // از سرایت رنگ دکمه به بقیه جلوگیری می‌کند
    }

    @Override
    public void dispose() {
        super.dispose();
        if (atlas != null) atlas.dispose();
    }
}
