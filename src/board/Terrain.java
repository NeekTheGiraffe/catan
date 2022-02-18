package board;

import economy.Resource;

import java.awt.*;

public enum Terrain {
    FOREST  (4, Resource.WOOD, new Color(15,115,55)),
    HILL    (3, Resource.BRICK, new Color(230,96,60)),
    FIELD   (4, Resource.WHEAT, new Color(252,212,33)),
    PASTURE (4, Resource.WOOL, new Color(31,207,75)),
    MOUNTAIN(3, Resource.ORE, new Color(143,143,143)),
    DESERT  (1, null, new Color(255, 238, 125));

    private int numTiles;
    private Resource resource;
    private Color color;

    Terrain(int numTiles, Resource r, Color c) {
        this.numTiles = numTiles;
        this.resource = r;
        this.color = c;
    }

    public int getNumTiles() {
        return numTiles;
    }
    public Resource getResource() {
        return resource;
    }
    public Color getColor() {
        return color;
    }
}
