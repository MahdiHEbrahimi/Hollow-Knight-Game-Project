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

        for (MapObject object : layer.getObjects()) {

            if (object instanceof RectangleMapObject) {

                Rectangle rect = ((RectangleMapObject) object).getRectangle();

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
                solidBlocks.add(new SolidBlock(rect.x, rect.y, rect.width, rect.height, isDeadly, type, respawnId));
            }
        }

        return solidBlocks;
    }
}
