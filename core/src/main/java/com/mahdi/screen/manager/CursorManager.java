package com.mahdi.screen.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;

public class CursorManager {
    private static CursorManager instance;

    // Store native OS-managed hardware cursor objects
    private Cursor normalCursor;
    private Cursor pointerCursor;

    private CursorManager() {
        // 1. Load the PNG images into temporary Pixmaps
        Pixmap normalPixmap = new Pixmap(Gdx.files.internal("global/cursor/custom_cursor.png"));
        Pixmap pointerPixmap = new Pixmap(Gdx.files.internal("global/cursor/custom_pointer.png"));

        // 2. Create native hardware cursors. (0,0) is the hotspot (the exact click point at top-left)
        normalCursor = Gdx.graphics.newCursor(normalPixmap, 0, 0);
        pointerCursor = Gdx.graphics.newCursor(pointerPixmap, 0, 0);

        // 3. Clean up the pixmaps from RAM immediately to prevent memory leaks
        normalPixmap.dispose();
        pointerPixmap.dispose();

        // 4. Apply the default game cursor right away globally
        setPointerMode(false);
    }

    public static CursorManager getInstance() {
        if (instance == null) {
            instance = new CursorManager();
        }
        return instance;
    }

    /**
     * Switches the global OS hardware cursor between normal arrow and hand pointer.
     * @param isPointer true for button hover hand, false for default arrow
     */
    public void setPointerMode(boolean isPointer) {
        if (isPointer && pointerCursor != null) {
            Gdx.graphics.setCursor(pointerCursor);
        } else if (normalCursor != null) {
            Gdx.graphics.setCursor(normalCursor);
        }
    }

    /**
     * Clears allocated hardware cursor resources from GPU memory on game exit.
     */
    public void dispose() {
        if (normalCursor != null) {
            normalCursor.dispose();
        }
        if (pointerCursor != null) {
            pointerCursor.dispose();
        }
    }
}