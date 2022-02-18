package economy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static game.Catan.*;

public enum Resource {
    WOOD    (0, new Color(79,117,40)),
    BRICK   (1, new Color(185,128,71)),
    WHEAT   (2, new Color(193,183,98)),
    WOOL    (3, new Color(145,187,105)),
    ORE     (4, new Color(116,125,125));

    Resource(int order, Color color) {
        this.order = order;
        this.color = color;
    }

    private final int order;
    private final Color color;
    private BufferedImage image;

    public BufferedImage getImage() {
        return image;
    }

    public Color getColor() {
        return color;
    }

    public int order() {
        return order;
    }

    static {
        try {
            WOOD.image = loadImage("wood.png");
            BRICK.image = loadImage("brick.png");
            WHEAT.image = loadImage("wheat.png");
            WOOL.image = loadImage("wool.png");
            ORE.image = loadImage("ore.png");
            System.out.println("Loaded images in Resources.java");
        } catch (IOException e) {
            System.err.println("Could not load images in Resources.java");
            e.printStackTrace();
        }
    }
}
