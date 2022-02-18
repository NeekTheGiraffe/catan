package cards;

import economy.HoldsCards;
import ui.Clickable;
import ui.Drawable;

public abstract class Card implements Drawable, Clickable {

    private HoldsCards owner;
    private int x;
    private int y;

    public static final int WIDTH = 37;
    public static final int HEIGHT = 60;

    Card(HoldsCards owner) {
        this.owner = owner;
        this.x = 0;
        this.y = 0;
    }

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public HoldsCards getOwner() {
        return owner;
    }
    public void setOwner(HoldsCards owner) {
        this.owner = owner;
    }

    public abstract String getName();
}
