package board;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.ArrayList;

public class Edge extends BoardElement {

    //protected int x;          // X component on coordinate grid
    //protected int y;          // Y component on coordinate grid
                                // x, y can range from 0 ... 4 * SIZE + 2
    private EdgeType edgeType;

    private Harbor harbor;      // Corresponding harbor
    private Road road;

    public Edge(int x, int y, Board board, EdgeType edgeType) {
        super(x, y, board);
        this.edgeType = edgeType;
        //System.out.println("Created board.Edge with parameters (" + this.x + ", " + this.y + ", "
                //+ edgeType + ")");
    }

    public enum EdgeType {
        RIGHT(1),
        UP   (2),
        LEFT (3);

        private final int code;

        EdgeType(int code) {
            this.code = code;
        }
        public int getCode() {
            return code;
        }

        private int[][] getPointsMatrix() {
            switch (this) {
                case RIGHT: return new int[][]{{-6, 5}, {44, 34}, {51, 21}, {1, -8}, {-6, 5}};
                case UP: return new int[][]{{7,0},{7,-55},{-7,-55},{-7,0},{7,0}};
                default: return new int[][]{{-1,-8},{-51,21},{-44,34},{6,5},{-1,-8}};
            }
        }
        private int[][] getHexMatrix() {
            switch (this) {
                case RIGHT: return new int[][]{{-1,1},{1,0}};
                case UP: return new int[][]{{0,-1},{-1,1}};
                default: return new int[][]{{0,-1},{1,0}};
            }
        }
        private int[][] getVertexMatrix(Vertex.VertexType vt) {
            switch (vt) {
                case UP: return new int[][]{{ 0, 0 }};
                default:
                    switch (this) {
                        case RIGHT: return new int[][]{{ 0, 1 }};
                        case UP: return new int[][]{{ -1, 0 }};
                        default: return new int[][]{{ 1, -1 }};
                    }
            }
        }
        private int[][] getEdgeMatrix(EdgeType neighborType) {
            switch (neighborType) {
                case RIGHT:
                    switch (this) {
                        case RIGHT: return new int[][]{};
                        case UP: return new int[][]{{ 0, 0 }, { -1, -1 }};
                        default: return new int[][]{{ 0, 0 }, { 1, -2 }};
                    }
                case UP:
                    switch (this) {
                        case RIGHT: return new int[][]{{ 0, 0 }, { 1, 1 }};
                        case UP: return new int[][]{};
                        default: return new int[][]{{ 0, 0 }, { 2, -1 }};
                    }
                default:
                    switch (this) {
                        case RIGHT: return new int[][]{{ 0, 0 }, { -1, 2 }};
                        case UP: return new int[][]{{ 0, 0 }, { -2, 1 }};
                        default: return new int[][]{};
                    }
            }
        }
    }

    public EdgeType getType() { return edgeType; }

    public Harbor getHarbor() {
        return harbor;
    }
    public void setHarbor(Harbor h) {
        if (harbor == null) {
            harbor = h;
        }
    }

    public boolean hasRoad() {
        return (road != null);
    }

    public Road getRoad() {
        return road;
    }

    public boolean setRoad(Road r) {
        if (road == null) {
            road = r;
            return true;
        }
        return false;
    }

    // Returns a list of neighboring Hexes
    @Override
    public List<Hex> getHexNeighbors() {

        return castToHex(getNeighbors(edgeType.getHexMatrix()));
    }
    // Returns a list of neighboring Vertices
    @Override
    public List<Vertex> getVertexNeighbors() {

        ArrayList<BoardElement> neighbors = new ArrayList<>();
        for (Vertex.VertexType vt : Vertex.VertexType.values()) {

            neighbors.addAll(getNeighbors(edgeType.getVertexMatrix(vt)));
        }
        return castToVertex(neighbors);
    }
    // Returns a list of neighboring Edges
    @Override
    public List<Edge> getEdgeNeighbors() {
        List<BoardElement> neighbors = new ArrayList<>();
        for (Edge.EdgeType et : Edge.EdgeType.values()) {

            neighbors.addAll(getNeighbors(edgeType.getEdgeMatrix(et), et.getCode()));
        }
        return castToEdge(neighbors);
    }

    // Specify to look at the up or down Vertex, then find that Vertex's Edge neighbors.
    // Exclude the Edge that called the method.
    public List<Edge> getVertexEdgeNeighbors(Vertex.VertexType type) {
        List<Edge> neighbors = getVertex(type).getEdgeNeighbors();
        neighbors.remove(this);
        return neighbors;
    }
    public Vertex getVertex(Vertex.VertexType type) {
        if (type == Vertex.VertexType.UP)
            return getUpVertex();
        else
            return getDownVertex();
    }
    public Vertex getUpVertex() {
        BoardElement be = getBoard().board[x][y][0];
        if (be instanceof Vertex)
            return (Vertex)be;
        return null;
    }
    public Vertex getDownVertex() {
        int[] vCoords = edgeType.getVertexMatrix(Vertex.VertexType.DOWN)[0];
        BoardElement be = getBoard().board[x+vCoords[0]][y+vCoords[1]][0];
        if (be instanceof Vertex)
            return (Vertex)be;
        return null;
    }

    @Override
    public boolean isInside(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int centerX = Board.ORIGINX + this.y * Board.SIZE60;
        int centerY = Board.ORIGINY + this.x * Board.SIZE90 + this.y * Board.SIZE30;
        int relX = x - centerX;
        int relY = y - centerY;
        switch (edgeType) {
            case UP: {
                return (relX >= -7 && relX <= 7 && relY >= -55 && relY <= 3);
            }
            case RIGHT: {
                if (relX <= 1) { // Left side
                    return (relY >= (int)(-(13.0/7.0) * (relX - 1.0) - 8.0)
                        && relY <= (int)((29.0/50.0) * (relX + 6.0) + 5.0));
                } else if (relX <= 44) { // Main shaft
                    return (relY >= (int)((29.0/50.0) * (relX - 1.0) - 8.0)
                            && relY <= (int)((29.0/50.0) * (relX + 6.0) + 5.0));
                } else { // Right side
                    return (relY >= (int)((29.0/50.0) * (relX - 1.0) - 8.0)
                            && relY <= (int)(-(13.0/7.0) * (relX - 44.0) + 34.0));
                }
            }
            default:
            case LEFT: {
                if (relX <= -44) { // Left side
                    return (relY >= (int)(-(29.0/50.0) * (relX + 51.0) + 21.0)
                            && relY <= (int)((13.0/7.0) * (relX + 51.0) + 21.0));
                } else if (relX <= -1) { // Main shaft
                    return (relY >= (int)(-(29.0/50.0) * (relX + 51.0) + 21.0)
                            && relY <= (int)(-(29.0/50.0) * (relX - 6.0) + 5.0));
                } else { // Right side
                    return (relY >= (int)((13.0/7.0) * (relX + 1.0) - 8.0)
                            && relY <= (int)(-(29.0/50.0) * (relX - 6.0) + 5.0));
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {

        int centerX = Board.ORIGINX + y * Board.SIZE60;
        int centerY = Board.ORIGINY + x * Board.SIZE90 + y * Board.SIZE30;

        GeneralPath edgePath = new GeneralPath();
        int[][] points = edgeType.getPointsMatrix();
        edgePath.moveTo(points[0][0] + centerX, points[0][1] + centerY);
        for (int i = 1; i < points.length; i++) {
            edgePath.lineTo(points[i][0] + centerX, points[i][1] + centerY);
        }
        edgePath.closePath();

        g2d.setStroke(new BasicStroke());

        // Draw the shape
        if (isHighlighted()) {
            g2d.setColor(getHighlight());
        } else if (hasRoad()) {
            g2d.setColor(road.getOwner().getColor());
        } else {
            g2d.setColor(new Color(255, 211, 130));
        }
        g2d.fill(edgePath);

    }

    @Override
    public String toString() {
        return "[Edge: x=" + x + ", y=" + y + ", type=" + edgeType + "]";
    }
}
