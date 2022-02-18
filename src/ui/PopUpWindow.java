package ui;

import game.Catan;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class PopUpWindow implements Drawable {

    public static final int MARGIN = 10;

    private final int x, y, width, height;
    private final Rectangle background;

    private boolean visible = true;

    private ArrayList<Drawable> components = new ArrayList<>();
    private ArrayList<Clickable> clickables = new ArrayList<>();

    public PopUpWindow(Catan game, Dimension size) {

        width = (int)size.getWidth();
        height = (int)size.getHeight();
        x = (game.getWidth() - width)/2;
        y = (game.getHeight() - height)/2;
        background = new Rectangle(x - MARGIN, y - MARGIN, width + 2*MARGIN, height + 2*MARGIN);
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public CButton getButton(String str) {
        for (Clickable c : clickables) {
            if (c instanceof CButton b) {
                if (b.getText().equals(str))
                    return b;
            }
        }
        throw new IllegalArgumentException("str: " + str);
    }
    public void show(String str) {
        getButton(str).setVisible(true);
    }
    public void hide(String str) {
        getButton(str).setVisible(false);
    }
    public void hideAllButtons() {
        for (Clickable c : clickables) {
            if (c instanceof CButton b)
                b.setVisible(false);
        }
    }
    public TextArea getTextArea(String str) {
        for (Drawable d : components) {
            if (d instanceof TextArea ta) {
                if (ta.getId().equals(str))
                    return ta;
            }
        }
        throw new IllegalArgumentException("str: " + str);
    }

    @Override
    public void draw(Graphics2D g2d) {

        if (!visible) return;
        g2d.setStroke(new BasicStroke());
        g2d.setColor(Color.black);
        g2d.draw(background);
        g2d.setColor(Color.gray);
        g2d.fill(background);

        for (Drawable d : components)
            d.draw(g2d);
    }

    public void add(Drawable d) {

        components.add(d);
        if (d instanceof Clickable)
            clickables.add((Clickable)d);
    }

    public Clickable getElementClicked(MouseEvent e) {

        if (!isVisible()) return null;
        for (Clickable c : clickables) {
            if (c.isInside(e))
                return c;
        }
        return null;
    }

    public boolean isVisible() {
        return visible;
    }
    public void show() {
        visible = true;
    }
    public void hide() {
        visible = false;
    }

    @Override
    public String toString() {
        return "[PopUpWindow]";
    }
}
