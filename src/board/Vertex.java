package board;

import game.Catan;
import game.Player;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class Vertex extends BoardElement {

    //protected int x;          // X component on coordinate grid
    //protected int y;          // Y component on coordinate grid
                                // x, y can range from 0 ... 4 * SIZE + 2
    private VertexType vertexType;
    private Settlement settlement;

    private static final int SIZE = 20;

    private static final int[][][] EDGE_POINTS = {
            {{-6,5}, {44,34}, {51,21}, {1,-8}, {-6,5}},     // -30 or 330 degrees
            {{1,8}, {51,-21}, {44,-34}, {-6, -5}, {1,8}},   // 30 deg
            {{7,3}, {7,-55}, {-7,-55}, {-7,3}, {7,3}},      // 90 deg
            {{6,-5}, {-44,-34}, {-51,-21}, {-1,8}, {6,-5}}, // 150 deg
            {{-1,-8}, {-51,21}, {-44,34}, {6,5}, {-1,-8}},  // 210 deg
            {{-7,-3}, {-7,55}, {7,55}, {7,-3}, {-7,-3}},    // 270 deg
    };
    // Regular triangle x,y coords
    private static final int[][] TRI_PTS = {
            {0, -10}, {9, 5}, {-9, 5}, {0, -10}
    };
    // Regular pentagon x,y coords
    private static final int[][] PENT_PTS = {
            {0, -10}, {10, -3}, {6, 8}, {-6, 8}, {-10, -3}, {0, -10}
    };

    public enum VertexType {
        UP  (0),
        DOWN(1);

        private int code;

        VertexType(int code) {
            this.code = code;
        }
        public int getCode() {
            return code;
        }

        // Returns relative positions of neighboring hexes
        private int[][] getHexMatrix() {
            switch (this) {
                case UP: return new int[][]{{0,-1},{1,0},{-1,1}};
                default: return new int[][]{{0,1},{-1,0},{1,-1}};
            }
        }

        // Returns relative positions of neighboring vertices
        private int[][] getVertexMatrix() {
            switch (this) {
                case UP: return new int[][]{{0,1},{-1,0},{1,-1}};
                default: return new int[][]{{0,-1},{1,0},{-1,1}};
            }
        }

        // Returns relative positions of neighboring edges
        private int[][] getEdgeMatrix(Edge.EdgeType et) {
            switch (this) {
                case UP: return new int[][]{{0, 0}};
                default:
                    switch (et) {
                        case RIGHT: return new int[][]{{0, -1}};
                        case UP: return new int[][]{{1, 0}};
                        default: return new int[][]{{-1, 1}};
                    }
            }
        }
    }

    public Vertex(int x, int y, Board board, VertexType type) {
        super(x, y, board);
        this.vertexType = type;
        settlement = null;
        //System.out.println("Created board.Vertex with parameters (" + this.x + ", " + this.y + ", "
                //+ vertexType + ")");
    }

    public boolean setSettlement(Settlement s) {
        if (settlement == null) {
            settlement = s;
            return true;
        }
        return false;
    }

    public boolean hasSettlement() {
        return (settlement != null);
    }

    public VertexType getVertexType() {
        return vertexType;
    }

    public Settlement getSettlement() {
        return settlement;
    }

    public Player getOwner() {
        if (settlement == null) return null;
        return settlement.getOwner();
    }

    public int getLevel() {
        return settlement.getLevel();
    }

    public boolean isOpen() {
        return getBoard().isOpen(this);
    }

    // Returns a list of neighboring Hexes
    @Override
    public List<Hex> getHexNeighbors() {

        return castToHex(getNeighbors(vertexType.getHexMatrix()));
    }
    // Returns a list of neighboring Vertices
    @Override
    public List<Vertex> getVertexNeighbors() {

        return castToVertex(getNeighbors(vertexType.getVertexMatrix()));
    }
    // Returns a list of neighboring Edges
    @Override
    public List<Edge> getEdgeNeighbors() {
        ArrayList<BoardElement> neighbors = new ArrayList<>();
        for (Edge.EdgeType et : Edge.EdgeType.values()) {

            neighbors.addAll(getNeighbors(vertexType.getEdgeMatrix(et), et.getCode()));
        }
        return castToEdge(neighbors);
    }

    @Override
    public boolean isInside(MouseEvent e) {
        int centerX = Board.ORIGINX + y * Board.SIZE60;
        int centerY = Board.ORIGINY + x * Board.SIZE90 + y * Board.SIZE30;
        double dist = Math.hypot(e.getX() - centerX, e.getY() - centerY);
        //System.out.println(dist);
        return (dist < (double)(SIZE) / 2.0);
    }

    // Draws a circle on the vertex
    @Override
    public void draw(Graphics2D g2d) {

        int centerX = Board.ORIGINX + y * Board.SIZE60;
        int centerY = Board.ORIGINY + x * Board.SIZE90 + y * Board.SIZE30;

        if (isHighlighted()) {
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(getHighlight());
        } else {
            g2d.setStroke(new BasicStroke());
            g2d.setColor(Color.black);
        }

        if (settlement == null) {
            g2d.drawOval(centerX-SIZE/2, centerY-SIZE/2, SIZE,SIZE);
            g2d.setColor(Color.white);
            g2d.fillOval(centerX-SIZE/2, centerY-SIZE/2, SIZE,SIZE);
        } else if (this.settlement.getLevel() == Settlement.SETTLEMENT) {
            GeneralPath triangle = new GeneralPath();
            triangle.moveTo(TRI_PTS[0][0] + centerX, TRI_PTS[0][1] + centerY);
            for (int i = 1; i < TRI_PTS.length; i++) {
                triangle.lineTo(TRI_PTS[i][0] + centerX, TRI_PTS[i][1] + centerY);
            }
            triangle.closePath();
            g2d.draw(triangle);
            g2d.setColor(settlement.getOwner().getColor());
            g2d.fill(triangle);
        } else { // Settlement is a city
            GeneralPath pentagon = new GeneralPath();
            pentagon.moveTo(PENT_PTS[0][0] + centerX, PENT_PTS[0][1] + centerY);
            for (int i = 1; i < PENT_PTS.length; i++) {
                pentagon.lineTo(PENT_PTS[i][0] + centerX, PENT_PTS[i][1] + centerY);
            }
            pentagon.closePath();
            g2d.draw(pentagon);
            g2d.setColor(settlement.getOwner().getColor());
            g2d.fill(pentagon);
        }
    }

    public void drawEdgeType(int type, Graphics2D g2d) {
        if (type < 0 || type > 5) return;
        int centerX = Board.ORIGINX + y * Board.SIZE60;
        int centerY = Board.ORIGINY + x * Board.SIZE90 + y * Board.SIZE30;

        GeneralPath edgePath = new GeneralPath();
        int[][] points = EDGE_POINTS[type];
        edgePath.moveTo(points[0][0] + centerX, points[0][1] + centerY);
        for (int i = 1; i < points.length; i++) {
            edgePath.lineTo(points[i][0] + centerX, points[i][1] + centerY);
        }
        edgePath.closePath();

        g2d.fill(edgePath);
    }
    public void drawEdge(int degrees, Graphics2D g2d) {
        if ((degrees + 30) % 60 != 0) return;
        drawEdgeType(((degrees + 30) / 60) % 6, g2d);
    }
    //FIXME: Make this more elegant
    public Point followEdge(int type) {
        if (type < 0 || type > 5) return null;
        return new Point(EDGE_POINTS[type][2][0], EDGE_POINTS[type][2][1]);
    }

    @Override
    public String toString() {
        return "[Vertex: x=" + x + ", y=" + y + "]";
    }
}
