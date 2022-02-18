package board;

import ui.Clickable;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public abstract class BoardElement implements Clickable {

    //FIXME: Make x and y private
    protected int x;    // X component on coordinate grid
    protected int y;    // Y component on coordinate grid
                        // x, y can range from 0 ... 4 * SIZE + 2
    private Color highlight;
    private final Board board; // NOTE: Change access modifier?

    protected BoardElement(int x, int y, Board board) {
        this.x = x;
        this.y = y;
        this.board = board;
        this.highlight = null;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public Board getBoard() {
        return board;
    }
    public Color getHighlight() {
        return highlight;
    }
    public void setHighlight(Color h) {
        highlight = h;
    }
    public void clearHighlight() {
        highlight = null;
    }
    public boolean isHighlighted() {
        return (highlight != null);
    }

    // Attempts to find elements on the grid with relative position(s) to the current one,
    // and optionally a specified z (when looking for edges).
    protected List<BoardElement> getNeighbors(int[][] relPos) {
        return getNeighbors(relPos, 0);
    }
    protected List<BoardElement> getNeighbors(int[][] relPos, int z) {
        BoardElement[][][] b = getBoard().board;
        List<BoardElement> list = new ArrayList<>();
        int bSizeX = b.length;
        int bSizeY = b[0].length;
        for (int[] relPo : relPos) {
            int neighborX = x + relPo[0];
            int neighborY = y + relPo[1];
            if (neighborX < 0 || neighborX >= bSizeX || neighborY < 0 || neighborY >= bSizeY)
                continue;
            if (b[neighborX][neighborY][z] == null)
                continue;
            list.add(b[neighborX][neighborY][z]);
        }
        return list;
    }

    protected List<Hex> castToHex(List<BoardElement> list) {
        List<Hex> hexes = new ArrayList<>();
        for (BoardElement be : list) {
            if (be instanceof Hex)
                hexes.add((Hex)be);
        }
        return hexes;
    }
    protected List<Vertex> castToVertex(List<BoardElement> list) {
        List<Vertex> vertices = new ArrayList<>();
        for (BoardElement be: list) {
            if (be instanceof Vertex)
                vertices.add((Vertex)be);
        }
        return vertices;
    }
    protected List<Edge> castToEdge(List<BoardElement> list) {
        List<Edge> edges = new ArrayList<>();
        for (BoardElement be : list) {
            if (be instanceof Edge)
                edges.add((Edge)be);
        }
        return edges;
    }

    public abstract List<Hex> getHexNeighbors();
    public abstract List<Vertex> getVertexNeighbors();
    public abstract List<Edge> getEdgeNeighbors();
    public abstract boolean isInside(MouseEvent e);

}
