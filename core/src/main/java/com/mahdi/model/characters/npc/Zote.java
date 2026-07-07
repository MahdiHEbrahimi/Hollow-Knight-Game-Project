package com.mahdi.model.characters.npc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mahdi.model.characters.Player;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.model.characters.BaseCharacter;

import java.util.Random;

/**
 * ☀️ زوت — NPC تعاملی. مستقیماً BaseCharacter را پیاده می‌کند (نه Enemy).
 * دیالوگ‌ها/قوانین زیر متنِ اورجینال با الهام از لحن شخصیت زوت‌اند، نه رونویسی از بازی اصلی.
 */
public class Zote extends BaseCharacter {

    private static TextureAtlas atlas;
    private final Random random = new Random();

    private final Animation<TextureRegion> animIdle;
    private final Animation<TextureRegion> animTalk;
    private final Animation<TextureRegion> animFall;
    private final Animation<TextureRegion> animGetUp;
    private final Animation<TextureRegion> animAttack;
    private final Animation<TextureRegion> animRoll;
    private final Animation<TextureRegion> animTurn;

    private enum ZState { IDLE, TALK, FALL, GET_UP, TURN, ANGRY_ROLL, ANGRY_ATTACK }
    private ZState state = ZState.IDLE;
    private float stateTime = 0f;

    private int facing = -1; // ☀️ فرض: آرت پیش‌فرض رو به چپه
    private final float mScale = 1.6f;

    private final Player player;
    private static final float INTERACT_RANGE = 220f;
    private static final float ANGRY_DURATION = 3f;
    private static final float ANGRY_HOP_FORCE = 650f; // ☀️ پرش نمادین وسط حمله (بدون انیمیشن جدا)
    private float angryTimer = 0f;

    // ===================== محتوای دیالوگ (اورجینال، نه رونویسی از بازی) =====================
    private static final String[] INTRO_LINES = {
        "Halt! You stand before Zote, greatest warrior this kingdom has never thanked.",
        "You have not heard of me? Strange. My legend echoes... somewhere, surely.",
        "Now be off. I have important matters. Standing, mostly."
    };
    private static final String[] PRECEPTS = {
        "Precept the First: Never trust a bug offering tea.",
        "Precept the Second: A true warrior complains before, during, and after battle.",
        "Precept the Third: If it shines, it is a trap. Unless it is treasure.",
        "Precept the Fourth: Sleep first, plan later, regret always.",
        "Precept the Fifth: A hero never apologizes, especially when wrong.",
        "Precept the Sixth: The ground is a resting place, even mid-battle.",
        "Precept the Seventh: If someone is smaller than you, they are beneath you."
    };

    private boolean introFinished = false;
    private int nextPreceptIndex = 0;

    private boolean dialogueActive = false;
    private String[] activeLines = null;
    private int lineIndex = -1;
    private int visibleChars = 0;
    private float typeTimer = 0f;
    private static final float CHARS_PER_SECOND = 28f;

    private static ShapeRenderer boxRenderer;
    private static Sound[] voiceSfx; // ☀️ فرض مسیر: SFX/Zote/zote_voice_1..3.wav
    private static final GlyphLayout layout = new GlyphLayout();

    public Zote(float x, float y, Player player) {
        super(x, y, 90, 120, 300f, 1200f, 999);
        this.player = player;
        this.hasGravity = true;

        if (atlas == null) atlas = new TextureAtlas("NPCs/Zote/Zote.atlas");

        animIdle   = anim("Idle", 0.18f, Animation.PlayMode.LOOP);
        animTalk   = anim("Talk", 0.12f, Animation.PlayMode.LOOP);
        animFall   = anim("Fall", 0.09f, Animation.PlayMode.NORMAL);
        animGetUp  = anim("Get Up", 0.09f, Animation.PlayMode.NORMAL);
        animAttack = anim("Attack", 0.07f, Animation.PlayMode.NORMAL);
        animRoll   = anim("Roll", 0.08f, Animation.PlayMode.NORMAL);
        animTurn   = anim("Turn", 0.1f, Animation.PlayMode.NORMAL);

        if (boxRenderer == null) boxRenderer = new ShapeRenderer();
        if (voiceSfx == null) {
            voiceSfx = new Sound[]{
                Gdx.audio.newSound(Gdx.files.internal("SFX/Zote/zote_voice_1.wav"))
            };
        }
    }

    private Animation<TextureRegion> anim(String prefix, float dur, Animation.PlayMode mode) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(prefix);
        if (regions.size == 0) {
            System.err.println("WARNING: Zote animation '" + prefix + "' not found!");
            return null;
        }
        return new Animation<>(dur, regions, mode);
    }

    @Override
    protected void updateCustomLogic(float delta) {
        stateTime += delta;
        isMoving = false;
        velocity.x = 0;

        handleInteractionInput();
        updateDialogueTyping(delta);
        checkPlayerAttackHit(); // ☀️ خودش هیت‌باکس پلیر رو با bounds خودش چک می‌کنه

        switch (state) {
            case FALL:
                if (isAnimFinished(animFall)) { state = ZState.GET_UP; stateTime = 0f; }
                break;
            case GET_UP:
                if (isAnimFinished(animGetUp)) { startAngry(); }
                break;
            case TURN:
                if (isAnimFinished(animTurn)) { state = ZState.ANGRY_ROLL; stateTime = 0f; }
                break;
            case ANGRY_ROLL:
                faceTowardsPlayer();
                velocity.x = facing * 260f; // شارژ به سمت پلیر، بدون دمیج
                isMoving = true;
                if (isAnimFinished(animRoll)) {
                    state = ZState.ANGRY_ATTACK;
                    stateTime = 0f;
                    if (isGrounded()) velocity.y = ANGRY_HOP_FORCE; // ☀️ پرش نمادین وسط حمله
                }
                break;
            case ANGRY_ATTACK:
                if (isAnimFinished(animAttack)) { state = ZState.ANGRY_ROLL; stateTime = 0f; }
                break;
            default:
                break;
        }

        if (state == ZState.ANGRY_ROLL || state == ZState.ANGRY_ATTACK || state == ZState.TURN) {
            angryTimer -= delta;
            if (angryTimer <= 0f) {
                state = ZState.IDLE;
                stateTime = 0f;
                velocity.x = 0;
            }
        }
    }

    private boolean isAnimFinished(Animation<TextureRegion> a) {
        return a == null || a.isAnimationFinished(stateTime);
    }

    private void faceTowardsPlayer() {
        facing = (player.getPosition().x > position.x) ? 1 : -1;
    }

    private boolean isReacting() {
        return state == ZState.FALL || state == ZState.GET_UP || state == ZState.TURN
            || state == ZState.ANGRY_ROLL || state == ZState.ANGRY_ATTACK;
    }

    /** ☀️ به‌جای وابستگی به فراخوانی بیرونی، خودش هیت‌باکس حمله‌ی پلیر رو چک می‌کنه */
    private void checkPlayerAttackHit() {
        if (isReacting()) return;
        Rectangle hitbox = player.getAttackHitbox();
        if (hitbox != null && hitbox.overlaps(bounds)) {
            onHitByPlayer();
        }
    }

    // ===================== تعامل / دیالوگ =====================
    private boolean isPlayerInRange() {
        return Math.abs(player.getPosition().x - position.x) <= INTERACT_RANGE;
    }

    private void handleInteractionInput() {
        if (dialogueActive) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                advanceDialogue();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                endDialogue(); // ☀️ E دوباره یعنی بستن فوری باکس
            }
            return;
        }
        if (isReacting()) return; // موقع زمین‌خوردن/عصبانیت نمی‌شه باهاش حرف زد
        if (isPlayerInRange() && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            startDialogue();
        }
    }

    private void startDialogue() {
        if (introFinished) {
            activeLines = new String[]{ PRECEPTS[nextPreceptIndex] };
            nextPreceptIndex = (nextPreceptIndex + 1) % PRECEPTS.length;
        } else {
            activeLines = INTRO_LINES;
        }
        dialogueActive = true;
        lineIndex = 0;
        beginLine();
        state = ZState.TALK;
        stateTime = 0f;
    }

    private void beginLine() {
        visibleChars = 0;
        typeTimer = 0f;
        if (voiceSfx != null && voiceSfx.length > 0) {
            voiceSfx[random.nextInt(voiceSfx.length)].play();
        }
    }

    private void advanceDialogue() {
        String line = activeLines[lineIndex];
        if (visibleChars < line.length()) {
            visibleChars = line.length(); // اینتر اول: نمایش فوری بقیه‌ی خط
            return;
        }
        lineIndex++;
        if (lineIndex >= activeLines.length) {
            endDialogue();
        } else {
            beginLine();
        }
    }

    private void endDialogue() {
        dialogueActive = false;
        activeLines = null;
        lineIndex = -1;
        introFinished = true;
        state = ZState.IDLE;
        stateTime = 0f;
        voiceSfx[0].stop();
    }

    private void updateDialogueTyping(float delta) {
        if (!dialogueActive) return;
        String line = activeLines[lineIndex];
        if (visibleChars < line.length()) {
            typeTimer += delta;
            visibleChars = Math.min(line.length(), (int) (typeTimer * CHARS_PER_SECOND));
        }
    }

    /** ☀️ Player باید حین دیالوگ حرکتش رو قفل کنه (این پرچم رو چک کنید) */
    public boolean isDialogueActive() {
        return dialogueActive;
    }

    /** ☀️ حالا خودِ Zote این رو از روی هیت‌باکس پلیر صدا می‌زنه؛ دیگه نیازی به وایر بیرونی نیست */
    public void onHitByPlayer() {
        if (isReacting()) return;
        dialogueActive = false;
        state = ZState.FALL;
        stateTime = 0f;
    }

    private void startAngry() {
        angryTimer = ANGRY_DURATION;
        faceTowardsPlayer();
        state = ZState.TURN;
        stateTime = 0f;
    }

    @Override
    public void die() {
        // زوت کشته نمی‌شه؛ فقط برای رعایت قرارداد BaseCharacter پیاده‌سازی شده
    }

    @Override
    public void draw(Batch batch) {
        Animation<TextureRegion> a;
        switch (state) {
            case TALK:         a = animTalk; break;
            case FALL:         a = animFall; break;
            case GET_UP:       a = animGetUp; break;
            case TURN:         a = animTurn; break;
            case ANGRY_ROLL:   a = animRoll; break;
            case ANGRY_ATTACK: a = animAttack; break;
            default:           a = animIdle; break;
        }
        if (a == null) a = animIdle;
        if (a == null) return;

        TextureRegion frame = a.getKeyFrame(stateTime, false);
        float w = frame.getRegionWidth() * mScale;
        float h = frame.getRegionHeight() * mScale;
        float dx = bounds.x + (bounds.width - w) / 2f;
        float dy = bounds.y;
        batch.draw(frame, dx, dy, w / 2f, h / 2f, w, h, facing == 1 ? -1 : 1, 1, 0);

        if (!dialogueActive && state == ZState.IDLE && isPlayerInRange()) {
            drawPrompt(batch);
        }
        if (dialogueActive) {
            drawDialogueBox(batch);
        }
    }

    private void drawPrompt(Batch batch) {
        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();
        font.setColor(Color.GOLD);
        font.draw(batch, "[E]", bounds.x + bounds.width / 2f - 20f, bounds.y + bounds.height + 180f);
        font.setColor(Color.WHITE);
    }

    private void drawDialogueBox(Batch batch) {
        String fullLine = activeLines[lineIndex];
        String shown = fullLine.substring(0, visibleChars);
        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();

        float boxW = 900f;
        float pad = 30f;
        float textAreaWidth = boxW - pad * 2f;

        // ☀️ ارتفاع بر اساس متنِ کامل خط حساب می‌شه (نه متن نصفه‌ی در حال تایپ)
        // تا وسط افکت تایپ‌رایتر باکس ناگهان بزرگ/کوچیک نشه.
        layout.setText(font, fullLine, Color.WHITE, textAreaWidth, Align.left, true);
        float boxH = Math.max(160f, layout.height + pad * 2f);

        float boxX = position.x - boxW / 2f;
        float boxY = bounds.y + bounds.height + 80f;

        batch.end();
        boxRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        boxRenderer.begin(ShapeRenderer.ShapeType.Filled);
        boxRenderer.setColor(0f, 0f, 0f, 0.75f);
        boxRenderer.rect(boxX, boxY, boxW, boxH);
        boxRenderer.end();
        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, shown, boxX + pad, boxY + boxH - pad, textAreaWidth, Align.left, true);
    }

    public static void disposeAtlas() {
        if (atlas != null) { atlas.dispose(); atlas = null; }
        if (voiceSfx != null) {
            for (Sound s : voiceSfx) if (s != null) s.dispose();
            voiceSfx = null;
        }
    }
}
