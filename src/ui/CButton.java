package ui;

import game.Catan;

import java.awt.*;
import java.awt.event.MouseEvent;

public class CButton implements Clickable {

    private Rectangle r;
    private String text;
    private String displayText;
    private boolean visible;
    private boolean selected;

    public CButton(Rectangle r, String text, String displayText) {
        this.r = r;
        this.text = text;
        this.displayText = displayText;
        this.visible = false;
        this.selected = false;
    }

    public CButton(Rectangle r, String text, String displayText, boolean vis) {
        this.r = r;
        this.text = text;
        this.displayText = displayText;
        this.visible = vis;
        this.selected = false;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean isInside(MouseEvent e) {
        if (!visible) return false;
        return r.contains(e.getPoint());
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void draw(Graphics2D g2d) {

        if (!visible) return;

        if (selected) {
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.yellow);
        } else {
            g2d.setStroke(new BasicStroke());
            g2d.setColor(Color.black);
        }
        g2d.draw(r);
        g2d.setColor(new Color(191, 97, 97));
        g2d.fill(r);
        g2d.setColor(Color.black);
        Font f = new Font("Purisa", Font.PLAIN, 16);
        Catan.drawCenteredString(g2d, displayText, r, f);

    }

    @Override
    public String toString() {
        return "[Button: " + text + "]";
    }
}
