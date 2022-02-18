package ui;

import game.Catan;

import java.awt.*;

public class TextArea implements Drawable {

    private final String id;
    private String str;
    private final Rectangle rect;

    public TextArea(String id, String str, Rectangle rect) {

        this.id = id;
        this.str = str;
        this.rect = rect;
    }

    public String getId() {
        return id;
    }

    public void setText(String str) {
        this.str = str;
    }


    @Override
    public void draw(Graphics2D g2d) {

        g2d.setColor(Color.black);
        Catan.drawCenteredString(g2d, str, rect, new Font("Purisa", Font.PLAIN, 18));
    }
}
