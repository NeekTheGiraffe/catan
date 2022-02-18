package ui;

import economy.Resource;

import java.awt.*;
import java.awt.event.MouseEvent;

public class ResourceIcon implements Clickable {

    public final Resource resource;

    private Rectangle rectangle;
    private boolean selected = false;

    public ResourceIcon(Resource res, Rectangle rect) {
        resource = res;
        rectangle = rect;
    }

    @Override
    public boolean isInside(MouseEvent e) {
        return rectangle.contains(e.getPoint());
    }

    @Override
    public void draw(Graphics2D g2d) {

        if (selected) {
            g2d.setColor(Color.yellow);
            g2d.setStroke(new BasicStroke(3));
        } else {
            g2d.setColor(Color.black);
            g2d.setStroke(new BasicStroke());
        }

        g2d.draw(rectangle);
        g2d.drawImage(resource.getImage(), rectangle.x, rectangle.y, rectangle.width, rectangle.height, null);

    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public boolean isSelected() {
        return selected;
    }
}
