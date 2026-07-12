package com.mahdi.screen.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.mahdi.model.enums.CheatCode;
import com.mahdi.model.enums.GameAction;
import com.mahdi.model.enums.MenuType;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.GameScreen;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.manager.PanelManager;
import com.mahdi.screen.ui.MenuButton;

public class GuidePanel extends BasePanel {

    private final BitmapFont font;
    private boolean toPauseMenu = false;

    // عرض ثابت برای تمام نوشته‌ها – با تغییر این عدد، همه‌جا اعمال می‌شود
    private static final float CONTENT_WIDTH = 2000;

    public GuidePanel(boolean toPauseMenu) {
        this();
        this.toPauseMenu = toPauseMenu;
    }

    public GuidePanel() {
        this.font = FontManager.getInstance().getEnglishMenuFont();

        setFillParent(true);
        align(Align.center);
        padTop(60f);

        // --- عنوان ---
        Label title = new Label("GUIDE", new Label.LabelStyle(font, Color.WHITE));  // رنگ صریح
        title.setFontScale(2.5f);
        add(title).padBottom(30f).row();

        // --- جدول محتوا (داخل اسکرول) ---
        Table content = new Table();
        content.align(Align.center);      // کل جدول وسط‌چین
        content.pad(30f);
        content.defaults().padBottom(10f);

        // ═══════════ کنترل‌ها ═══════════
        Label controlsHeader = new Label("--- CONTROLS ---", new Label.LabelStyle(font, Color.WHITE));
        controlsHeader.setFontScale(1.6f);
        controlsHeader.setAlignment(Align.center);
        content.add(controlsHeader).center().padBottom(15f).row();

        for (GameAction action : GameAction.values()) {
            String line = String.format("%-20s : [ %s ]", action.name(), action.getKeyName());
            Label lbl = new Label(line, new Label.LabelStyle(font, Color.WHITE));
            lbl.setAlignment(Align.center);
            lbl.setWrap(false);
            content.add(lbl).width(CONTENT_WIDTH).center().padBottom(5f).row();
        }

        content.row().padTop(25f);

        // ═══════════ توانایی‌ها ═══════════
        Label abilitiesHeader = new Label("--- ABILITIES ---", new Label.LabelStyle(font, Color.WHITE));
        abilitiesHeader.setFontScale(1.6f);
        abilitiesHeader.setAlignment(Align.center);
        content.add(abilitiesHeader).center().padBottom(15f).row();

        String[] abilities = {
            "Attack      : Strike enemies with your nail.",
            "Dash        : Quick horizontal dodge.",
            "Jump        : Jump. Press again in air for Double Jump.",
            "Focus       : Hold to heal using collected Soul.",
            "Wall Slide  : Touch a wall and hold direction to slide.",
            "Pogo        : Strike downward while in air to bounce off enemies.",
            "Soul        : Hitting enemies fills your Soul vessel.",
            "Health      : You have 5 masks. Use Focus to restore them."
        };

        for (String ab : abilities) {
            Label lbl = new Label("  " + ab, new Label.LabelStyle(font, Color.WHITE));
            lbl.setWrap(true);
            lbl.setAlignment(Align.center);
            content.add(lbl).width(CONTENT_WIDTH).center().padBottom(6f).row();
        }

        content.row().padTop(25f);

        // ═══════════ کدهای تقلب ═══════════
        Label cheatsHeader = new Label("--- CHEAT CODES ---", new Label.LabelStyle(font, Color.WHITE));
        cheatsHeader.setFontScale(1.6f);
        cheatsHeader.setAlignment(Align.center);
        content.add(cheatsHeader).center().padBottom(15f).row();

        for (CheatCode cheat : CheatCode.values()) {
            String text = String.format("[%s] %-20s : %s",
                cheat.getKeyName(), cheat.getDisplayName(), cheat.getDescription());
            Label lbl = new Label(text, new Label.LabelStyle(font, Color.WHITE));
            lbl.setWrap(true);
            lbl.setAlignment(Align.center);
            content.add(lbl).width(CONTENT_WIDTH).center().padBottom(6f).row();
        }

        // --- اسکرول ---
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        add(scroll).expand().fill().center().padBottom(20f).row();

        // --- دکمه بازگشت ---
        MenuButton backBtn = new MenuButton("BACK", font, () -> {
            if (!toPauseMenu)
                AppStatus.ChangeMenuAndPanel(MenuType.MAIN_MENU);
            else
                PanelManager.getInstance().performPanelTransition(new PausePanel((GameScreen) AppStatus.getScreen()));
        });
        add(backBtn).padBottom(30f).row();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
