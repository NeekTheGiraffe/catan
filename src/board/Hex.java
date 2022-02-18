package board;

import economy.Resource;
import game.Catan;
import game.Player;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class Hex extends BoardElement {

    //protected int x;          // X component on coordinate grid
    //protected int y;          // Y component on coordinate grid
                                // x, y can range from 0 ... 4 * SIZE + 2

    private Terrain terrain;    // Terrain of the hex
    private int diceValue;      // Sum of two dice rolls 2 ... 12

    // Drawing points of a regular hexagon
    private static final int points[][] = {
            { 43, 25 }, { 0, 50 }, { -43, 25 },
            { -43, -25 }, { 0, -50 }, { 43, -25 } , { 43, 25 }
    };
    // RELATIVE x,y coordinates of neighboring hexes
    private static final int HEX_NEIGHBORS[][] = {{1,1},{-1,-1},{2,-1},{-2,1},{-1,2},{1,-2}};
    // RELATIVE x,y coordinates of neighboring vertices
    private static final int VERTEX_NEIGHBORS[][] = {{1,0},{0,1},{-1,1},{-1,0},{0,-1},{1,-1}};
    // RELATIVE x,y coordinates of neighboring edges
    private int[][] getEdgeMatrix(Edge.EdgeType et) {
        switch (et) {
            case RIGHT: return new int[][]{{-1, 0}, {1, -1}};
            case UP: return new int[][]{{0, 1}, {1, -1}};
            default: return new int[][]{{0, 1}, {-1, 0}};
        }
    }

    public Hex(int x, int y, Board board, Terrain terrain, int diceValue) {
        super(x, y, board);
        this.terrain = terrain;
        this.diceValue = (diceValue >= 2 && diceValue <= 12) ? diceValue : 0;
        //System.out.println("Created board.Hex with parameters (" + this.x + ", " + this.y + ", "
                //+ this.resource.toString() + ", " + this.diceValue + ")");
    }

    public Resource getResource() {
        return terrain.getResource();
    }
    public boolean hasResource() {
        return (terrain.getResource() != null);
    }
    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }
    public int getDiceValue() {
        return diceValue;
    }
    public void setDiceValue(int diceValue) {
        this.diceValue = diceValue;
    }

    // Returns true if this hex contains the robber.
    public boolean hasRobber() {

        return this.equals(getBoard().getRobber().getHex());
    }

    public List<Player> getPlayerNeighbors() {
        ArrayList<Vertex> vertexNeighbors = (ArrayList<Vertex>) getVertexNeighbors();
        ArrayList<Player> playerNeighbors = new ArrayList<>();
        for (Vertex v : vertexNeighbors) {
            if (!v.hasSettlement()) continue;
            if (playerNeighbors.contains(v.getSettlement().getOwner())) continue;
            playerNeighbors.add(v.getSettlement().getOwner());
        }
        return playerNeighbors;
    }

    // Returns all neighboring Hexes
    @Override
    public List<Hex> getHexNeighbors() {

        return castToHex(getNeighbors(HEX_NEIGHBORS));
    }
    // Returns all neighboring Vertices
    @Override
    public List<Vertex> getVertexNeighbors() {

        return castToVertex(getNeighbors(VERTEX_NEIGHBORS));
    }
    // Returns all neighboring Edges
    @Override
    public List<Edge> getEdgeNeighbors() {

        ArrayList<BoardElement> neighbors = new ArrayList<>();
        for (Edge.EdgeType et : Edge.EdgeType.values()) {
            neighbors.addAll(getNeighbors(getEdgeMatrix(et), et.getCode()));
        }
        return castToEdge(neighbors);
    }

    @Override
    public boolean isInside(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int centerX = Board.ORIGINX + this.y * Board.SIZE60;
        int centerY = Board.ORIGINY + this.x * Board.SIZE90 + this.y * Board.SIZE30;
        //System.out.print("Checking Hex at " + this.x + ", " + this.y + ": ");
        if (y - centerY < -25) { // Upper two triangles
            //System.out.print("Upper triangles\n");
            if (x < centerX) {  // Upper left triangle
                return (y - centerY >= (int)(-(9.0/16.0) * (double)(x - centerX) - 50.0));
            } else {            // Upper right triangle
                return (y - centerY >= (int)((9.0/16.0) * (double)(x - centerX) - 50.0));
            }
        } else if (y - centerY < 25) { // Main rectangle
            //System.out.print("Main rectangle\n");
            return (x - centerX >= -43 && x - centerX <= 43);
        } else {                // Bottom two triangles
            //System.out.print("Bottom triangles\n");
            if (x < centerX) {  // Bottom left triangle
                return (y - centerY <= (int)((9.0/16.0) * (double)(x - centerX) + 50.0));
            } else {            // Bottom right triangle
                return (y - centerY <= (int)(-(9.0/16.0) * (double)(x - centerX) + 50.0));
            }
        }
    }

    // Draws a hexagon shape on the panel
    // FIXME: Find a way to clean up
    @Override
    public void draw(Graphics2D g2d) {

        int centerX = Board.ORIGINX + y * Board.SIZE60;
        int centerY = Board.ORIGINY + x * Board.SIZE90 + y * Board.SIZE30;

        GeneralPath hexagon = new GeneralPath();
        hexagon.moveTo(points[0][0] + centerX, points[0][1] + centerY);
        for (int i = 1; i < points.length; i++) {
            hexagon.lineTo(points[i][0] + centerX, points[i][1] + centerY);
        }
        hexagon.closePath();


        if (isHighlighted()) {
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(getHighlight());
        } else {
            g2d.setStroke(new BasicStroke());
            g2d.setColor(Color.black);
        }
        // Draw the hex
        g2d.draw(hexagon);
        g2d.setColor(terrain.getColor());
        g2d.fill(hexagon);

        // Draw dice value
        g2d.setStroke(new BasicStroke());
        g2d.setColor(Color.black);
        if (diceValue >= 2 && diceValue <= 12) {
            g2d.setColor(Color.black);
            g2d.drawOval(centerX - 15, centerY - 15, 30, 30);

            g2d.setColor(Color.white);
            g2d.fillOval(centerX - 15, centerY - 15, 30, 30);

            Font f = new Font("Purisa", Font.PLAIN, 18);
            g2d.setFont(f);
            Color c = (diceValue == 6 || diceValue == 8) ? Color.red : Color.black;
            g2d.setColor(c);
            Rectangle r = new Rectangle(centerX - 15, centerY - 15, 30, 30);
            Catan.drawCenteredString(g2d, Integer.toString(diceValue), r, f);

            if (hasRobber()) {
                g2d.setStroke(new BasicStroke(3));
                g2d.setColor(Color.red);
                g2d.drawLine(centerX - 11, centerY - 11, centerX + 11, centerY + 11);
                g2d.drawLine(centerX - 11, centerY + 11, centerX + 11, centerY - 11);
            }
        }

        // Draw the robber
        if (hasRobber()) {
            g2d.setStroke(new BasicStroke());
            g2d.setColor(Color.black);
            g2d.drawOval(centerX - 10, centerY + 18, 20, 20);
            g2d.setColor(Color.gray);
            g2d.fillOval(centerX - 10, centerY + 18, 20, 20);
        }
    }

    @Override
    public String toString() {
        return "[Hex: x=" + x + ", y=" + y + ", terrain=" + terrain + ", diceValue=" + diceValue + "]";
    }
}
