package game;

import economy.HoldsCards;
import economy.Resource;
import economy.Stockpile;
import ui.*;
import ui.TextArea;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Trade implements Event {

    private static final int OFFSET = 60;

    private PopUpWindow tradeWindow;
    private PopUpWindow offerWindow;
    private Catan game;

    private final Map<Resource, Integer> give = new HashMap<>();
    private final Map<Resource, Integer> get = new HashMap<>();
    private final Player actor;
    private int currentPlayer = -1;
    private final List<Player> players;

    private boolean tradeAccepted = false;
    private boolean maritime = false;
    private int phaseCode = Events.MORE_ACTIONS;

    public Trade(Catan game) {

        this.game = game;
        this.actor = game.currentPlayer();
        this.players = new ArrayList<>(game.getPlayers());
        players.remove(actor);
        initializeWindows();

        for (Resource res : Resource.values()) {
            give.put(res, 0);
            get.put(res, 0);
        }
    }

    @Override
    public boolean onClick(Clickable c) {

        if (c instanceof CButton b) {

            // Maritime trade toggle button
            if (b.getText().equals("maritime")) {
                if (maritime) {
                    b.setSelected(false);
                    maritime = false;
                } else {
                    b.setSelected(true);
                    maritime = true;
                }

                // Adjust number of the GET field if > numNotOwned
                for (Resource res : Resource.values()) {
                    int numNotOwned = numNotOwned(res);
                    if (get.get(res) > numNotOwned)
                        get.replace(res, numNotOwned);
                }
                return true;
            }

            // Cancel button
            if (b.getText().equals("cancel")) {
                phaseCode = Events.DONE;
                return true;
            }

            // Confirm button
            if (b.getText().equals("confirm")) {

                if (maritime) {
                    doTradeWith(game.getStockpile());
                    phaseCode = Events.DONE;
                    return true;
                }

                phaseCode = Events.CHANGE_PLAYER;
                tradeWindow.hide();
                buildOfferWindow();
                return true;
            }

            // Decline button
            if (b.getText().equals("decline")) {

                phaseCode = Events.CHANGE_PLAYER;
                return true;
            }

            // Accept button
            if (b.getText().equals("accept")) {

                doTradeWith(currentPlayer());
                tradeAccepted = true;
                phaseCode = Events.CHANGE_PLAYER;
                return true;
            }

            // Resource plus-minus buttons
            for (Resource res : Resource.values()) {

                boolean affectsGive = false;
                int newVal = -1;

                if (b.getText().equals(res + "-give+")) {
                    newVal = give.get(res) + 1;
                    affectsGive = true;
                }
                if (b.getText().equals(res + "-give-")) {
                    newVal = give.get(res) - 1;
                    affectsGive = true;
                }
                if (b.getText().equals(res + "-get+")) {
                    newVal = get.get(res) + 1;
                }
                if (b.getText().equals(res + "-get-")) {
                    newVal = get.get(res) - 1;
                }

                if (newVal == -1)
                    continue;

                if (affectsGive)
                    give.replace(res, newVal);
                else
                    get.replace(res, newVal);

                return true;
            }
        }

        return false;
    }

    @Override
    public void onScreenClick() {

    }

    @Override
    public int status() {
        return phaseCode;
    }

    @Override
    public Player nextPlayer() {
        if (phaseCode != Events.CHANGE_PLAYER) return null;

        currentPlayer++;
        if (currentPlayer == players.size() || tradeAccepted) {
            phaseCode = Events.DONE;
            return actor;
        }

        phaseCode = Events.MORE_ACTIONS;
        return players.get(currentPlayer);
    }

    @Override
    public Player currentPlayer() {
        if (currentPlayer == -1)
            return actor;
        return players.get(currentPlayer);
    }

    @Override
    public String message() {
        return "";
    }

    @Override
    public void updateButtons(MenuManager menus) {

        if (tradeWindow.isVisible()) {
            tradeWindow.hideAllButtons();
            for (Resource res : Resource.values()) {
                int playerCount = actor.count(res);
                boolean givePlusVisible = false;
                boolean getPlusVisible = false;

                int giveCount = give.get(res);
                int getCount = get.get(res);
                String s;

                if (giveCount > 0)
                    tradeWindow.show(res + "-give-");
                if (giveCount < playerCount && getCount == 0) {
                    tradeWindow.show(res + "-give+");
                    givePlusVisible = true;
                }
                s = (giveCount == 0 && !givePlusVisible) ? "" : Integer.toString(giveCount);
                tradeWindow.getTextArea(res + "-give-label").setText(s);


                if (getCount > 0)
                    tradeWindow.show(res + "-get-");
                if (getCount < numNotOwned(res) && giveCount == 0) {
                    tradeWindow.show(res + "-get+");
                    getPlusVisible = true;
                }

                s = (getCount == 0 && !getPlusVisible) ? "" : Integer.toString(getCount);
                tradeWindow.getTextArea(res + "-get-label").setText(s);
            }
            tradeWindow.show("maritime");
        }

        if (offerWindow != null && offerWindow.isVisible()) {
            if (canAcceptTrade())
                offerWindow.show("accept");
            else
                offerWindow.hide("accept");
        }

        if (currentPlayer == -1) {
            menus.show("cancel");
            if (validTrade())
                menus.show("confirm");
        }
    }

    @Override
    public void onFinish(Events events) {

        game.getMenus().remove(tradeWindow);
        game.getMenus().remove(offerWindow);
    }

    private void initializeWindows() {

        tradeWindow = tradeWindowSkeleton(false);

        // Plus-minus buttons
        for (Resource res : Resource.values()) {
            int x = tradeWindow.getX() + res.order() * 50 + 50;
            int y = tradeWindow.getY() + 50;

            Rectangle rect = new Rectangle(x, y, 18, 18);
            tradeWindow.add(new CButton(rect, res + "-give+", "+", true));
            rect = (Rectangle)rect.clone();
            rect.translate(0, 23);
            tradeWindow.add(new CButton(rect, res + "-give-", "-", true));

            rect = (Rectangle)rect.clone();
            rect.translate(0, OFFSET - 23);
            tradeWindow.add(new CButton(rect, res + "-get+", "+", true));
            rect = (Rectangle)rect.clone();
            rect.translate(0, 23);
            tradeWindow.add(new CButton(rect, res + "-get-", "-", true));
        }

        // Maritime trade label
        Rectangle r = new Rectangle(tradeWindow.getX()+300, tradeWindow.getY()+57, 80, 35);
        tradeWindow.add(new CButton(r, "maritime", "Maritime", true));

        game.getMenus().add(tradeWindow);
    }

    private int numNotOwned(Resource res) {
        if (maritime)
            return game.getStockpile().count(res);
        else
            return Stockpile.NUM_EACH_RESOURCE - actor.count(res) - game.getStockpile().count(res);
    }

    private boolean validMaritimeTrade() {
        int credit = 0;
        int getAmount = 0;
        for (Resource res : Resource.values()) {
            int offered = give.get(res);
            int price = actor.maritimePrice(res);

            if (offered % price != 0)
                return false;
            credit += offered / price;

            getAmount += get.get(res);
        }
        return (credit > 0 && credit == getAmount);
    }

    private boolean validDomesticTrade() {
        boolean giveB = false;
        boolean getB = false;
        for (Resource res : Resource.values()) {
            if (give.get(res) > 0)
                giveB = true;
            if (get.get(res) > 0)
                getB = true;
            if (giveB && getB) // Change this to (giveB) if "gifts" are allowed
                return true;
        }
        return false;
    }

    private boolean validTrade() {
        if (maritime)
            return validMaritimeTrade();
        return validDomesticTrade();
    }

    private void buildOfferWindow() {

        offerWindow = tradeWindowSkeleton(true);
        // Set the appropriate text for each resource
        for (Resource r : Resource.values()) {

            Integer actorGets = get.get(r);
            String sActorGets = offerString(actorGets);
            offerWindow.getTextArea(r + "-give-label").setText(sActorGets);

            Integer actorGives = give.get(r);
            String sActorGives = offerString(actorGives);
            offerWindow.getTextArea(r + "-get-label").setText(sActorGives);
        }

        // Accept/decline buttons
        Rectangle rect = new Rectangle(offerWindow.getX() + 60, offerWindow.getY() + 160, 80, 35);
        offerWindow.add(new CButton(rect, "accept", "Accept", true));
        rect = new Rectangle(offerWindow.getX() + 160, offerWindow.getY() + 160, 80, 35);
        offerWindow.add(new CButton(rect, "decline", "Decline", true));

        // Add to the menu manager
        game.getMenus().add(offerWindow);
    }

    private String offerString(Integer val) {

        return (val != 0) ? val.toString() : "";
    }

    private PopUpWindow tradeWindowSkeleton(boolean isOffer) {

        int width = (isOffer) ? 300 : 380;
        int height = (isOffer) ? 195 : 150;
        PopUpWindow puw = new PopUpWindow(game, new Dimension(width, height));

        // Give/get labels
        Rectangle r = new Rectangle(puw.getX(), puw.getY() + 50, 50, 30);
        puw.add(new TextArea("give-label","Give: ", r));
        r = new Rectangle(puw.getX(), puw.getY() + 50 + OFFSET, 50, 30);
        puw.add(new TextArea("get-label","Get: ", r));

        for (Resource res : Resource.values()) {

            int x = puw.getX() + res.order() * 50 + 50;
            int y = puw.getY();

            // Resource icons
            Rectangle rect = new Rectangle(x, y, 40, 40);
            ResourceIcon icon = new ResourceIcon(res, rect);
            puw.add(icon);

            // GIVE +/- labels
            Rectangle rect2 = (!isOffer) ? new Rectangle(x + 20, y + 50, 20, 40)
                    : new Rectangle (x, y + 50, 40, 40);
            String s = "a";
            puw.add(new TextArea(res + "-give-label", s, rect2));

            // GET +/- labels
            rect2 = (Rectangle)rect2.clone();
            rect2.translate(0, OFFSET);
            puw.add(new TextArea(res + "-get-label", s, rect2));
        }
        return puw;
    }

    private void doTradeWith(HoldsCards other) {
        if (other.equals(actor))
            throw new IllegalArgumentException("Cannot trade with self");
        for (Map.Entry<Resource, Integer> entry : give.entrySet()) {
            actor.give(entry.getKey(), entry.getValue(), other);
        }
        for (Map.Entry<Resource, Integer> entry : get.entrySet()) {
            actor.take(entry.getKey(), entry.getValue(), other);
        }
    }

    private boolean canAcceptTrade() {
        for (Resource r : Resource.values()) {
            if (currentPlayer().count(r) < get.get(r))
                return false;
        }
        return true;
    }
}
