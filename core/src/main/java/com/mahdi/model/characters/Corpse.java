// package com.mahdi.model.characters;

// import com.badlogic.gdx.graphics.g2d.Batch;
// import com.badlogic.gdx.graphics.g2d.TextureRegion;

// public class Corpse extends BaseCharacter {

//     private final TextureRegion corpseTexture;
//     private float fadeTimer = 3.0f; // جسد ۳ ثانیه روی زمین می‌ماند و بعد محو می‌شود

//     public Corpse(float x, float y, float width, float height, TextureRegion corpseTexture, float deathKnockbackX) {
//         super(x, y, width, height);
//         this.corpseTexture = corpseTexture;
        
//         // ایجاد افکت پرتاب شدن جسد به عقب بر اساس جهت ضربه شمشیر شوالیه
//         this.velocity.x = deathKnockbackX;
//         this.velocity.y = 400f; // یک پرش کوچک رو به بالا موقع مرگ برای حس طبیعی‌تر
//         this.hasGravity = true;
//     }

//     @Override
//     protected void updateCustomLogic(float delta) {
//         // اصطکاک روی زمین: سرعت افقی جسد آرام‌آرام کم شود تا بایستد
//         if (isGrounded) {
//             velocity.x *= 0.85f; 
//         }

//         // تایمر محو شدن جسد
//         fadeTimer -= delta;
//         if (fadeTimer <= 0) {
//             this.isAlive = false; // ماشه حذف نهایی از لیست بازی توسط GameStatus
//         }
//     }

//     @Override
//     public void draw(Batch batch) {
//         if (corpseTexture == null) return;

//         // اعمال افکت غیب شدن تدریجی (Fade out) روی آلفای رنگِ بچ گرافیکی
//         if (fadeTimer < 1.0f) {
//             batch.setColor(1, 1, 1, fadeTimer); // کم کردن شفافیت در ثانیه آخر
//         }

//         // رسم جسد
//         batch.draw(corpseTexture, position.x, position.y, bounds.width, bounds.height);

//         // ریست کردن رنگ بچ به حالت عادی برای اینکه بقیه بازی خراب نشود
//         batch.setColor(1, 1, 1, 1);
//     }
// }