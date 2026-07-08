package com.mahdi.screen.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.mahdi.model.enums.Charm;
import com.mahdi.screen.GameScreen;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.ui.MenuButton;

import java.util.ArrayList;
import java.util.List;

public class InventoryPanel extends BasePanel {

    private final BitmapFont font;
    private TextureAtlas charmAtlas;
    private final GameScreen gameScreen;

    // ترتیب فعال‌سازی چارم‌ها (قدیمی‌ترین در اندیس ۰) – استاتیک تا با دیسپوز پنل از بین نرود
    private static final List<Charm> activeOrder = new ArrayList<>();

    public InventoryPanel(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.font = FontManager.getInstance().getEnglishMenuFont();
        charmAtlas = new TextureAtlas("HUD/charms.atlas");

        setFillParent(true);
        align(Align.top);
        padTop(60f);

        buildUI();
    }

    private void buildUI() {
        this.clearChildren();

        // عنوان
        Label title = new Label("CHARMS", new Label.LabelStyle(font, Color.WHITE));
        title.setFontScale(2.2f);
        add(title).padBottom(20f).row();

        // توضیح محدودیت ناچ
        Label notchLabel = new Label("Notch Limit: 3 (oldest charm will be replaced if exceeded)",
            new Label.LabelStyle(font, Color.WHITE));
        notchLabel.setFontScale(1f);
        add(notchLabel).padBottom(20f).row();

        // جدول چارم‌ها
        Table charmsTable = new Table();
        charmsTable.align(Align.center);
        charmsTable.defaults().space(12f).center();

        for (Charm charm : Charm.values()) {
            // ۱. دریافت تصویر (اسم فایل = displayName بدون فاصله)
            String regionName = charm.getDisplayName().replace(" ", "");
            TextureRegion region = charmAtlas.findRegion(regionName);
            if (region == null) {
                // تلاش با نام enum (حروف کوچک)
                region = charmAtlas.findRegion(charm.name().toLowerCase());
            }
            if (region == null) continue;

            boolean active = charm.isActive();

            // ۲. تصویر چارم (رنگ مناسب)
            Image img = new Image(region);
            img.setAlign(Align.center);
            img.setColor(active ? Color.WHITE : new Color(0.35f, 0.35f, 0.35f, 0.65f));

            // ۳. نام و توضیح – رنگ صریح سفید برای جلوگیری از تغییر ناخواسته
            Label nameLabel = new Label(charm.getDisplayName(),
                new Label.LabelStyle(font, Color.WHITE));
            nameLabel.setFontScale(1.2f);
            nameLabel.setAlignment(Align.left);

            Label descLabel = new Label(charm.getDescription(),
                new Label.LabelStyle(font, Color.WHITE));
            descLabel.setFontScale(0.9f);
            descLabel.setAlignment(Align.left);

            Table textRow = new Table();
            textRow.align(Align.left);
            textRow.add(nameLabel).padRight(15f);
            textRow.add(descLabel).expandX().left();

            // ۴. ردیف کامل: تصویر + متن (قابل کلیک برای فعال/غیرفعال)
            Table row = new Table();
            row.align(Align.center);
            row.add(img).size(120, 120).center();
            row.add(textRow).padLeft(20f).expandX().left();

            // کلیک روی کل ردیف منجر به تغییر وضعیت چارم شود
            row.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    toggleCharm(charm);
                    refreshUI();
                }
            });

            charmsTable.add(row).center().padBottom(10f).row();
        }

        // قرار دادن جدول در یک ScrollPane برای اسکرول
        ScrollPane scrollPane = new ScrollPane(charmsTable);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);   // فقط اسکرول عمودی
        add(scrollPane).expand().fill().padBottom(15f).row();

        // دکمه بازگشت
        MenuButton backButton = new MenuButton("BACK", font, gameScreen::resumeGame);
        add(backButton).padBottom(25f).row();
    }

    /**
     * تغییر وضعیت یک چارم با رعایت محدودیت سه ناچ.
     */
    private void toggleCharm(Charm charm) {
        if (charm.isActive()) {
            charm.setActive(false);
            activeOrder.remove(charm);
        } else {
            if (activeOrder.size() >= 3) {
                Charm oldest = activeOrder.remove(0);
                oldest.setActive(false);
            }
            charm.setActive(true);
            activeOrder.add(charm);
        }
    }

    /**
     * بازسازی پنل برای نمایش وضعیت جدید چارم‌ها.
     */
    private void refreshUI() {
        buildUI();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.setColor(Color.WHITE);   // بازنشانی رنگ بچ
    }

    @Override
    public void dispose() {
        super.dispose();
        if (charmAtlas != null) charmAtlas.dispose();
    }
}
