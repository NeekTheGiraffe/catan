package board;

import economy.Resource;
import game.Catan;
import ui.Clickable;
import ui.Drawable;

import java.awt.*;
import java.awt.event.MouseEvent;

public class Harbor implements Drawable, Clickable {

    private final int drawingHint;
    private final Edge edge;
    private Resource resource;

    private static final int SIZE = 30;

    public static final int DRAW_RIGHT = 1;
    public static final int DRAW_LEFT = 0;

    private static final int[][] DRAWING_MAP = {
            {5,4}, {1,2}, {3,4}, {1,0}, {3,2}, {5,0}
    };

    public Harbor(Edge edge, Board board, int drawingHint) {
        this.edge = edge;
        this.drawingHint = (drawingHint == DRAW_RIGHT) ? DRAW_RIGHT : DRAW_LEFT;
        resource = null;
        edge.setHarbor(this);
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    //FIXME: Finish isInside
    @Override
    public boolean isInside(MouseEvent e) {
        return false;
    }

    //FIXME: Clean this up
    @Override
    public void draw(Graphics2D g2d) {
        // Draw the docks
        g2d.setColor(new Color(92, 66, 7));
        int index = 2*(edge.getType().getCode() - 1) + drawingHint;
        //System.out.println("Attempting to draw harbor (" + x + ", " + y + ", "
        //        + edge.getType() + ", " + drawingHint + ") using DRAWING_MAP[" + index + "]");
        edge.getUpVertex().drawEdgeType(DRAWING_MAP[index][0], g2d);
        edge.getDownVertex().drawEdgeType(DRAWING_MAP[index][1], g2d);

        // Draw a rectangle with corresponding resource color
        Point p = edge.getUpVertex().followEdge(DRAWING_MAP[index][0]);
        int centerX = Board.ORIGINX + edge.getY() * Board.SIZE60 + (int)p.getX();
        int centerY = Board.ORIGINY + edge.getX() * Board.SIZE90 + edge.getY() * Board.SIZE30 + (int)p.getY();

        g2d.setStroke(new BasicStroke());
        g2d.setColor(Color.black);
        g2d.drawRect(centerX - SIZE/2, centerY - SIZE/2, SIZE, SIZE);


        // Draw corresponding resource
        if (resource == null) {
            g2d.setColor(Color.white);
            g2d.fillRect(centerX - SIZE/2, centerY - SIZE/2, SIZE, SIZE);
            g2d.setColor(Color.black);
            Font f = new Font("Purisa", Font.PLAIN, 16);
            g2d.setFont(f);
            Catan.drawCenteredString(g2d, "?", new Rectangle(centerX,centerY,0,0), f);
        } else {
            g2d.drawImage(resource.getImage(), centerX - SIZE/2, centerY - SIZE/2, SIZE, SIZE, null);
        }
    }
}
