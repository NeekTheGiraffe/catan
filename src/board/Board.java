package board;

import economy.Resource;
import economy.Robber;
import game.Catan;
import ui.Drawable;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Board implements Drawable {

    BoardElement[][][] board;
    private ArrayList<Hex> hexes = new ArrayList<>();
    private ArrayList<Vertex> vertices = new ArrayList<>();
    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<BoardElement> boardElements = new ArrayList<>();

    private ArrayList<Vertex> settleableVertices = new ArrayList<>();

    private ArrayList<Harbor> harbors = new ArrayList<>();

    private Robber robber;

    public final Catan game;

    public static final int ORIGINX = 200;
    public static final int ORIGINY = -100;//Where (0,0) on the board should be drawn
    public static final int SIZE90 = 55; // Total size of a hex including margins
    public static final int SIZE60 = 48; // SIZE90 * sqrt(3)/2
    public static final int SIZE30 = 28; // SIZE90 * 1/2

    public static final int DIE_SIDES = 6;
    public static final int DICE_MIN = 2;
    public static final int DICE_MAX = DIE_SIDES * 2;
    public static final int DICE_COUNTS[] = { // Starts at 2 and goes to 12 (11 values)
            1, 2, 2, 2, 2, 0, 2, 2, 2, 2, 1
    };

    public Board(Catan game) {

        this.game = game;
        initializeBoard(2);
    }
    public Board(Catan game, int size) {

        this.game = game;
        initializeBoard(size);
    }

    public List<Hex> getHexes() {
        return hexes;
    }
    public List<Vertex> getVertices() {
        return vertices;
    }
    public List<Edge> getEdges() {
        return edges;
    }
    public List<BoardElement> getBoardElements() {
        return boardElements;
    }
    public Robber getRobber() {
        return robber;
    }

    public BoardElement at(int x, int y) {
        return board[x][y][0];
    }

    public Edge edgeAt(int x, int y, Edge.EdgeType type) {
        BoardElement be = board[x][y][type.getCode()];
        if (be instanceof Edge)
            return (Edge)be;
        return null;
    }
    public Edge rightEdgeAt(int x, int y) {
        return edgeAt(x, y, Edge.EdgeType.RIGHT);
    }
    public Edge upEdgeAt(int x, int y) {
        return edgeAt(x, y, Edge.EdgeType.UP);
    }
    public Edge leftEdgeAt(int x, int y) {
        return edgeAt(x, y, Edge.EdgeType.LEFT);
    }

    public BoardElement elementClickedOn(MouseEvent e) {
        for (Vertex v : vertices) { // Vertices have click priority
            if (v.isInside(e)) return v;
        }
        for (Hex h : hexes) { //
            if (h.isInside(e)) return h;
        }
        for (Edge edge : edges) {
            if (edge.isInside(e)) return edge;
        }
        return null;
    }

    public boolean isOpen(Vertex v) {
        updateSettlements();

        for (Vertex v1 : settleableVertices) {
            if (v.equals(v1))
                return true;
        }
        return false;
    }

    public void clearAllHighlights() {
        for (BoardElement be : boardElements)
            be.clearHighlight();
    }
    public void highlightOpenVertices(Color c) {
        updateSettlements();

        for (Vertex v : settleableVertices) {
            v.setHighlight(c);
        }
    }

    public void drawHexes(Graphics2D g) {
        for (Hex h : hexes) {
            h.draw(g);
        }
    }
    public void drawVertices(Graphics2D g) {
        for (Vertex v : vertices) {
            v.draw(g);
        }
    }
    public void drawEdges(Graphics2D g) {
        for (Edge e : edges) {
            e.draw(g);
        }
    }
    public void drawHarbors(Graphics2D g) {
        for (Harbor h : harbors) {
            h.draw(g);
        }
    }
    @Override
    public void draw(Graphics2D g) {
        drawHarbors(g);
        drawEdges(g);
        drawHexes(g);
        drawVertices(g);
    }

    // Checks if any "settleable" vertices have been settled.
    // If they have been, removes them & their neighbors from the pool.
    private void updateSettlements() {
        for (int i = 0; i < settleableVertices.size(); i++) {
            Vertex v = settleableVertices.get(i);
            if (!v.hasSettlement()) continue;

            // Remove the settled vertex
            settleableVertices.remove(v);
            // If neighbors were settleable, they aren't anymore
            ArrayList<Vertex> neighbors = (ArrayList<Vertex>)v.getVertexNeighbors();
            for (Vertex v2 : neighbors)
                settleableVertices.remove(v2);
            i--;
        }
    }


    private void initializeBoard(int size) {

        board = new BoardElement[11][11][4];
        // Create hexes
        //FIXME: Clean this shit up
        int startingX = -4;
        for (int diagonal = -2; diagonal <= 2; diagonal++) {
            int diagSize = 5 - Math.abs(diagonal);

            for (int x = startingX; x < startingX + diagSize; x++) {
                Hex h = new Hex(x+5, x-3*diagonal+5, this, Terrain.DESERT, 0);
                board[x+5][x-3*diagonal+5][0] = h;
                hexes.add(h);
                boardElements.add(h);
            }
            startingX += (diagonal < 0) ? 1 : 2;
        }
        System.out.println("Created all hexes");

        // Initialize hexes with a resource
        ArrayList<Hex> emptyHexes = (ArrayList<Hex>)hexes.clone();
        ArrayList<Hex> resourceHexes = new ArrayList<>();
        for (Terrain t : Terrain.values()) {
            // Assign t to a random desert tile
            for (int k = 0; k < t.getNumTiles(); k++) {
                int index = (int)(emptyHexes.size() * Math.random());
                Hex h = emptyHexes.get(index);
                h.setTerrain(t);
                if (t.getResource() != null)
                    resourceHexes.add(h);
                emptyHexes.remove(index);

                // Assign the robber to the first desert hex
                if (k == 0 && t == Terrain.DESERT) {
                    robber = new Robber(h);
                    System.out.println("Created the robber");
                }
            }
        }
        System.out.println("Assigned resources to each hex");

        // Initialize resource hexes with dice values
        for (int diceValue = DICE_MIN; diceValue <= DICE_MAX; diceValue++) {
            for (int k = 0; k < DICE_COUNTS[diceValue - DICE_MIN]; k++) {
                int index = (int)(resourceHexes.size() * Math.random());
                Hex h = resourceHexes.get(index);
                h.setDiceValue(diceValue);
                resourceHexes.remove(index);
            }
        }
        System.out.println("Assigned dice values to each resource hex");

        // Initialize DOWN vertices
        // FIXME: Clean this up
        startingX = -5;
        for (int diagonal = -3; diagonal <= 2; diagonal++) {
            int diagSize = 6 - Math.abs(diagonal);
            for (int x = startingX; x < startingX + diagSize; x++) {
                Vertex v = new Vertex(x+5, x-3*diagonal+4, this, Vertex.VertexType.DOWN);
                board[x+5][x-3*diagonal+4][0] = v;
                vertices.add(v);
                settleableVertices.add(v);
                boardElements.add(v);
            }
            startingX += (diagonal < 0) ? 1 : 2;
        }

        // Initialize UP vertices and EDGES
        //FIXME: Clean this up
        startingX = -5;
        for (int diagonal = -2; diagonal <= 3; diagonal++) {
            int diagSize = 6 - Math.abs(diagonal);
            for (int x = startingX; x < startingX + diagSize; x++) {
                int y = x-3*diagonal+1;
                Vertex v = new Vertex(x+5, y+5, this, Vertex.VertexType.UP);
                board[x+5][y+5][0] = v;
                vertices.add(v);
                settleableVertices.add(v);
                boardElements.add(v);

                for (Edge.EdgeType et : Edge.EdgeType.values()) {
                    // Omit certain edges if they are on the edge of the board.
                    if (et == Edge.EdgeType.RIGHT && x+2*y == 8) continue;
                    if (et == Edge.EdgeType.UP && 2*x+y == -8) continue;
                    if (et == Edge.EdgeType.LEFT && x - y == 8) continue;
                    Edge e = new Edge(x+5, y+5, this, et);
                    board[x+5][y+5][et.getCode()] = e;
                    edges.add(e);
                    boardElements.add(e);
                }
            }
            startingX += (diagonal < 0) ? 1 : 2;
        }
        System.out.println("Created all vertices and edges");

        //FIXME: Improve mechanism of adding harbors
        addHarbor(10,2, 1,0);
        addHarbor(8,6, 3,1);
        addHarbor(5,9, 3,1);
        addHarbor(3,10, 2,1);
        addHarbor(1,8, 1,1);
        addHarbor(1,5, 1,1);
        addHarbor(2,3, 3,0);
        addHarbor(6,1, 2,0);
        addHarbor(9,1, 2,0);
        System.out.println("Created all harbors");

        ArrayList<Harbor> genericHarbors = (ArrayList<Harbor>)harbors.clone();
        for (Resource r : Resource.values()) {
            int index = (int)(genericHarbors.size() * Math.random());
            Harbor h = genericHarbors.get(index);
            h.setResource(r);
            genericHarbors.remove(index);
        }
        System.out.println("Assigned resources to each harbor");
    }
    private void addHarbor(int x, int y, int z, int drawingHint) {
        harbors.add(new Harbor((Edge)board[x][y][z], this, drawingHint));
    }
}
