package game;

import board.Edge;
import ui.CButton;
import ui.Clickable;

import java.awt.*;
import java.util.List;

public class RoadBuilding implements Event {

    private final int NUM_TO_BUILD = 2;
    private int numBuilt = 0;

    private List<Edge> validEdges;

    private Player actor;
    private Edge edge;

    public RoadBuilding(Catan game) {
        actor = game.currentPlayer();
        updateValidEdges();
    }

    @Override
    public boolean onClick(Clickable c) {

        if (c instanceof CButton) {
            CButton b = (CButton) c;
            if (!b.getText().equals("confirm")) return false;
            edge.clearHighlight();
            actor.buildRoad(edge);

            numBuilt++;
            edge = null;
            updateValidEdges();
            return true;
        }

        if (c instanceof Edge) {
            Edge e = (Edge) c;
            if (!validEdges.contains(e)) return false;
            if (e.equals(edge)) return false;

            if (edge != null)
                edge.setHighlight(Color.yellow);
            edge = e;
            edge.setHighlight(actor.getSecondaryColor());
            return true;
        }
        return false;
    }

    @Override
    public void onScreenClick() {

    }

    @Override
    public int status() {
        if (numBuilt == NUM_TO_BUILD || validEdges.size() == 0)
            return Events.DONE;
        return Events.MORE_ACTIONS;
    }

    @Override
    public Player nextPlayer() {
        return null;
    }


    @Override
    public Player currentPlayer() {
        return actor;
    }

    @Override
    public String message() {
        return "Click on an edge to build a road there (" + (numBuilt+1) + "/" + NUM_TO_BUILD + ")";
    }

    @Override
    public void updateButtons(MenuManager menus) {
        if (edge != null)
            menus.show("confirm");
    }

    @Override
    public void onFinish(Events events) {

    }

    private void updateValidEdges() {
        validEdges = actor.getBuildableEdges();
        for (Edge e : validEdges)
            e.setHighlight(Color.yellow);
    }
}
