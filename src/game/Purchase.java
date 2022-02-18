package game;

import board.*;
import cards.DevelopmentCard;
import economy.Stockpile;
import ui.CButton;
import ui.Clickable;

import java.awt.*;


public class Purchase implements Event {

    public static final int ROAD = 1;
    public static final int SETTLEMENT = 2;
    public static final int CITY = 3;
    public static final int DEV_CARD = 4;

    private final Player actor;
    private final Board board;
    private final Stockpile stockpile;
    private int purchaseType;
    private boolean boughtYet;

    private Edge edge;
    private Vertex vertex;
    private String message;

    public Purchase(Catan game) {
        this.actor = game.currentPlayer();
        this.board = game.getBoard();
        this.stockpile = game.getStockpile();
        this.purchaseType = 0;
        this.message = "";
    }

    //FIXME: Move this logic to the Purchasable classes
    @Override
    public boolean onClick(Clickable c) {

        if (c instanceof CButton) {
            CButton b = (CButton) c;
            switch (b.getText()) {
                case "road":
                    prepareToBuy(ROAD);
                    break;
                case "settlement":
                    prepareToBuy(SETTLEMENT);
                    break;
                case "city":
                    prepareToBuy(CITY);
                    break;
                case "development-card":
                    prepareToBuy(DEV_CARD);
                    break;
                case "cancel":
                    cancelBuy();
                    break;
                case "confirm":
                    finishPurchase();
                default:
                    return false;
            }
            return true;
        }

        Vertex v; // Vertex that was clicked on
        Edge e;   // Edge that was clicked on
        Player p = currentPlayer(); // Current player
        switch (purchaseType) {
            case ROAD:
                if (!(c instanceof Edge)) return false;
                e = (Edge) c;
                if (!p.canBuildOn(e)) return false; // Player must choose neighboring road

                if (edge != null) edge.setHighlight(Color.yellow);
                edge = e;
                edge.setHighlight(p.getSecondaryColor());
                return true;
            case SETTLEMENT:
                if (!(c instanceof Vertex)) return false;
                v = (Vertex) c;
                if (!p.canSettle(v)) return false;

                if (vertex != null) vertex.setHighlight(Color.yellow);
                vertex = v;
                vertex.setHighlight(p.getSecondaryColor());
                return true;
            case CITY:
                if (!(c instanceof Vertex)) return false;
                v = (Vertex) c;
                if (!p.canUpgrade(v)) return false;

                if (vertex != null) vertex.setHighlight(Color.yellow);
                vertex = v;
                vertex.setHighlight(p.getSecondaryColor());
                return true;
        }
        return false;
    }

    @Override
    public int status() {
        return (boughtYet) ? Events.DONE : Events.MORE_ACTIONS;
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
        return message;
    }

    @Override
    public void updateButtons(MenuManager menus) {
        menus.show("cancel");
        switch (purchaseType) {
            case ROAD:
                if (edge != null)
                    menus.show("confirm");
                break;
            case SETTLEMENT:
            case CITY:
                if (vertex != null)
                    menus.show("confirm");
                break;
            case DEV_CARD:
                menus.show("confirm");
                break;
            default:
                menus.show("road");
                menus.show("settlement");
                menus.show("city");
                menus.show("development-card");
                break;
        }
    }

    @Override
    public void onScreenClick() {

    }
    @Override
    public void onFinish(Events events) {

    }

    private void prepareToBuy(int code) {
        if (code < 1 || code > 4)
            return;
        switch (code) {
            case ROAD:
                if (actor.getBuildableEdges().size() == 0) {
                    message = "There are no edges that you can build on!";
                    return;
                }
                if (!actor.canAfford(Road.price())) {
                    message = "A road costs 1 wood and 1 brick.";
                    return;
                }
                for (Edge e : currentPlayer().getBuildableEdges())
                    e.setHighlight(Color.yellow);
                message = "Click on an empty edge to build there";
                break;
            case SETTLEMENT:
                if (actor.getSettleableVertices().size() == 0) {
                    message = "There are no intersections that you can settle on!";
                    return;
                }
                if (!actor.canAfford(Settlement.price())) {
                    message = "A settlement costs 1 each of wood, brick, wheat & wool.";
                    return;
                }
                for (Vertex v : currentPlayer().getSettleableVertices())
                    v.setHighlight(Color.yellow);
                message = "Click on an open intersection to settle there";
                break;
            case CITY:
                if (!actor.canAfford(Settlement.cityPrice())) {
                    message = "A city costs 2 wheat and 3 ore.";
                    return;
                }
                for (Vertex v : currentPlayer().getUpgradableVertices())
                    v.setHighlight(Color.yellow);
                message = "Click on a settlement to upgrade it";
                break;
            case DEV_CARD:
                if (stockpile.numDevCards() == 0) {
                    message = "There are no development cards left to buy!";
                    return;
                }
                if (!actor.canAfford(DevelopmentCard.price())) {
                    message = "A development card costs 1 each of wheat, wool, & ore.";
                    return;
                }
                message = "Click on the confirm button to buy a card";
                break;
        }
        purchaseType = code;
    }

    private void cancelBuy() {
        purchaseType = 0;
        board.clearAllHighlights();
        edge = null;
        vertex = null;
        boughtYet = true;
    }

    private void finishPurchase() {
        Player p = currentPlayer();
        switch (purchaseType) {
            case ROAD:
                edge.clearHighlight();
                p.pay(Road.price()); // Guaranteed to be able to afford b/c of prepareToBuy()
                p.buildRoad(edge);
                System.out.println(p + " builds a road");
                break;
            case SETTLEMENT:
                vertex.clearHighlight();
                p.pay(Settlement.price());
                p.settle(vertex);
                System.out.println(p + " builds a settlement");
                break;
            case CITY:
                vertex.clearHighlight();
                p.pay(Settlement.cityPrice());
                p.upgrade(vertex);
                System.out.println(p + " upgrades a settlement");
                break;
            case DEV_CARD:
                p.pay(DevelopmentCard.price());
                p.takeDevCard();
                System.out.println(p + " buys a development card");
                break;
        }
        boughtYet = true;
    }
}
