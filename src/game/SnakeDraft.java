package game;

import board.Board;
import board.Edge;
import board.Vertex;
import ui.CButton;
import ui.Clickable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static board.Edge.EdgeType.*;

public class SnakeDraft implements Event {

    private static final int[][] Q_START_SET = {
            {3,4},{7,2},{2,7},{7,5},
            {8,3},{6,7},{5,3},{4,8}
    };
    private static final int[][] Q_START_ROADS = {
            {3,4},{7,2},{2,6},{7,5},
            {8,3},{6,7},{5,3},{4,8}
    };
    private static final Edge.EdgeType[] Q_START_RTYPES = {
            RIGHT, RIGHT, RIGHT, RIGHT,
            RIGHT, UP,    LEFT,  UP
    };

    private List<Player> players;
    private List<Edge> validEdges;
    private Board board;
    private int numSettlements;
    private int index;
    private String msg;

    private Vertex vertex;
    private Edge edge;
    private Catan game;

    private int phaseCode = Events.MORE_ACTIONS;
    private boolean started = false;

    public SnakeDraft(List<Player> players, int numSettlements, Board board, Catan game) {

        this.index = 0;
        this.players = (ArrayList<Player>) players;
        this.numSettlements = numSettlements;
        this.msg = "Click on an intersection & edge to build there";
        this.board = board;
        this.game = game;

        highlightVerticesOnly();
    }

    @Override
    public Player nextPlayer() {
        if (phaseCode != Events.CHANGE_PLAYER) return null;

        phaseCode = 0;
        if (currentPlayer().numSettlements() % 2 == 1) {
            if (index < players.size() - 1)
                index++;
        } else {
            if (index > 0)
                index--;
        }
        return currentPlayer(); // The next player in the draft
    }

    @Override
    public Player currentPlayer() {
        return players.get(index);
    }

    @Override
    public String message() {
        return msg;
    }
    @Override
    public boolean onClick(Clickable c) {

        if (c instanceof CButton b) {
            if (b.getText().equals("confirm")) {
                currentPlayer().settle(vertex);
                currentPlayer().buildRoad(edge);
                if (endOfDraft())
                    phaseCode = Events.DONE;
                else
                    phaseCode = Events.CHANGE_PLAYER;
                vertex = null;
                edge = null;
                validEdges = null; // Unnecessary
                highlightVerticesOnly();
                return true;
            }
            if (b.getText().equals("draft-quick-start")) {
                quickStart();
                phaseCode = Events.DONE;
                return true;
            }
            return false;
        }

        started = true;

        if (c instanceof Vertex v) {

            if (!v.isOpen()) return false;      // Clicked on an occupied vertex
            if (v.equals(vertex)) return false; // Clicked on the same vertex

            vertex = v;
            edge = null; // Any previous edge is no longer valid

            validEdges = vertex.getEdgeNeighbors();
            highlightEdgesOnly();
            vertex.setHighlight(currentPlayer().getSecondaryColor());
            return true;
        }
        if (c instanceof Edge e) {

            if (vertex == null) return false; // Clicked on a road while the settlement was null
            if (e.equals(edge)) return false; // Clicked on the same edge
            if (!validEdges.contains(e)) return false; // Edge does not neighbor the vertex

            edge = e;

            board.clearAllHighlights();
            vertex.setHighlight(currentPlayer().getSecondaryColor());
            edge.setHighlight(currentPlayer().getSecondaryColor());
            return true;
        }

        return false;
    }

    @Override
    public int status() {
        return phaseCode;
    }

    @Override
    public void updateButtons(MenuManager menus) {
        if (!started) {
            menus.show("draft-quick-start");
            return;
        }
        if (vertex != null && edge != null)
            menus.setVisible("confirm", true);
    }

    private boolean endOfDraft() {
        if (currentPlayer().numSettlements() != numSettlements)
            return false;
        if (numSettlements % 2 == 1) {
            return (index == players.size() - 1);
        } else {
            return (index == 0);
        }
    }

    private void highlightEdgesOnly() {
        board.clearAllHighlights();
        for (Edge e : validEdges)
            e.setHighlight(Color.yellow);
    }
    private void highlightVerticesOnly() {
        board.clearAllHighlights();
        board.highlightOpenVertices(Color.yellow);
    }

    @Override
    public void onScreenClick() {

    }

    @Override
    public void onFinish(Events events) {
        //Give initial resources to all the players
        game.giveInitialResources();
        events.add(new Turn(game, events));
    }

    private void quickStart() {
        int i = 0;
        for (Player p : players) {
            for (int j = 0; j < 2; j++) {
                p.settle((Vertex) board.at(Q_START_SET[i][0], Q_START_SET[i][1]));
                p.buildRoad(board.edgeAt(Q_START_ROADS[i][0], Q_START_ROADS[i][1], Q_START_RTYPES[i]));
                i++;
            }
        }
    }
}
