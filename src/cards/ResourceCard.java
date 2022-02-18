package cards;

import economy.HoldsCards;
import economy.Resource;
import game.Player;

import java.awt.*;
import java.awt.event.MouseEvent;

public class ResourceCard extends Card {

    private Resource resource;
    private boolean selected;

    public ResourceCard(HoldsCards owner, Resource r) {
        super(owner);
        this.resource = r;
        this.selected = false;
    }


    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public Resource getResource() {
        return resource;
    }

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
        g2d.setColor(resource.getColor());
        g2d.fill(r);
        g2d.drawImage(resource.getImage(), getX()+2, getY()+14, 33, 33, null);
    }

    @Override
    public String getName() {
        return resource.toString();
    }

    @Override
    public boolean isInside(MouseEvent e) {
        if (!(getOwner() instanceof Player)) return false;
        Player p = (Player) getOwner();
        if (!p.isActive()) return false;

        return (e.getX() >= getX() && e.getX() <= getX() + Card.WIDTH
            && e.getY() >= getY() && e.getY() <= getY() + Card.HEIGHT);
    }

    @Override
    public String toString() {
        return "[Resource Card: " + resource.toString() + "]";
    }
}
