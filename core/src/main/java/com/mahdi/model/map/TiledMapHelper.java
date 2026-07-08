package com.mahdi.model.map;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class TiledMapHelper {
    private TiledMap tiledMap;

    public TiledMap loadMap(String path) {
        tiledMap = new TmxMapLoader().load(path);
        return tiledMap;
    }

    public Array<SolidBlock> getSolidRectangles() {
        Array<SolidBlock> solidBlocks = new Array<>();
        MapLayer layer = tiledMap.getLayers().get("logical");
        if (layer == null) return solidBlocks;

        int mapHeightInTiles = tiledMap.getProperties().get("height", Integer.class);
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);
        float totalMapHeight = mapHeightInTiles * tileHeight;

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                // تبدیل Y (Tiled پایین-چپ → LibGDX بالا-چپ)
                float x = rect.x;
                float y = rect.y;

                boolean isDeadly = false;
                if (object.getProperties().containsKey("deadly")) {
                    isDeadly = object.getProperties().get("deadly", Boolean.class);
                }

                String type = "wall";
                if (object.getProperties().containsKey("type")) {
                    type = object.getProperties().get("type", String.class);
                }

                int respawnId = -1;
                if (object.getProperties().containsKey("respawn")) {
                    respawnId = object.getProperties().get("respawn", Integer.class);
                }

                // پراپرتی‌های جدید
                String musicPath = null;
                if (object.getProperties().containsKey("musicPath")) {
                    musicPath = object.getProperties().get("musicPath", String.class);
                }

                String mapPath = null;
                if (object.getProperties().containsKey("mapPath")) {
                    mapPath = object.getProperties().get("mapPath", String.class);
                }

                solidBlocks.add(new SolidBlock(x, y, rect.width, rect.height,
                    isDeadly, type, respawnId, musicPath, mapPath));
            }
        }
        return solidBlocks;
    }
}
