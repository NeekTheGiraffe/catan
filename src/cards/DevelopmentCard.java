package cards;

import economy.HoldsCards;
import economy.Resource;
import game.Catan;
import game.Player;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import game.Event;

public abstract class DevelopmentCard extends Card {

    private boolean selected;

    private static final HashMap<Resource, Integer> PRICE;

    public abstract Event getEvent(Catan game);
    public abstract BufferedImage getImage();
    public abstract String toString();
    public abstract boolean playable();

    public DevelopmentCard(HoldsCards owner) {
        super(owner);
    }

    // Set price of a development card
    static {
        PRICE = new HashMap<>();
        PRICE.put(Resource.WHEAT, 1);
        PRICE.put(Resource.WOOL, 1);
        PRICE.put(Resource.ORE, 1);
        System.out.println("Assigned price for development cards");
    }

    public static HashMap<Resource, Integer> price() {
        return PRICE;
    }

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void draw(Graphics2D g2d) {

        if (selected) {
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.yellow);
        } else {
            g2d.setStroke(new BasicStroke());
            g2d.setColor(Color.black);
        }
        Rectangle r = new Rectangle(getX(), getY(), Card.WIDTH, Card.HEIGHT);
        g2d.draw(r);
        g2d.setColor(Color.white);
        g2d.fill(r);
        g2d.drawImage(getImage(), getX()+1, getY()+13, 35, 35, null);
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public boolean isInside(MouseEvent e) {
        if (!(getOwner() instanceof Player)) return false;
        Player p = (Player) getOwner();
        if (!p.isActive()) return false;

        return (e.getX() >= getX() && e.getX() <= getX() + Card.WIDTH
                && e.getY() >= getY() && e.getY() <= getY() + Card.HEIGHT);
    }
}
