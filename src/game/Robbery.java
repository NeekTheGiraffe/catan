package game;

import cards.ResourceCard;
import ui.CButton;
import ui.Clickable;

import java.util.ArrayList;
import java.util.List;

public class Robbery implements Event {

    private final ArrayList<Player> robbedPlayers;
    private final Player origPlayer;
    private int index;
    private String message;

    private int phaseCode = 0;

    public Robbery(List<Player> players, Player currentPlayer, int robbedThreshold) {
        origPlayer = currentPlayer;
        robbedPlayers = new ArrayList<>();
        for (Player p : players) {
            if (p.numResources() >= robbedThreshold)
                if (p.equals(currentPlayer))
                    robbedPlayers.add(0, p);
                else
                    robbedPlayers.add(p);
        }
        index = 0;
        if (robbedPlayers.size() != 0)
            updateMessage();
        else
            phaseCode = Events.DONE;
    }

    private int numToDiscard() {
        return currentPlayer().numResources() / 2;
    }

    public Player currentPlayer() {
        if (robbedPlayers.size() == 0)
            return null;
        return robbedPlayers.get(index);
    }

    private void updateMessage() {
        message = "You are being robbed! Click on " + numToDiscard() + " cards to discard them";
    }

    @Override
    public boolean onClick(Clickable c) {

        if (c instanceof CButton) {
            CButton b = (CButton) c;
            if (b.getText().equals("confirm")) {
                System.out.println(currentPlayer() + " is robbed and discards " + numToDiscard() + " resources");
                currentPlayer().discardSelectedCards();

                phaseCode = Events.CHANGE_PLAYER;
                return true;
            }
            return false;
        }

        if (!(c instanceof ResourceCard)) return false;
        ResourceCard rc = (ResourceCard)c;
        rc.setSelected(!rc.isSelected());
        return true;
    }

    @Override
    public Player nextPlayer() {
        index++;
        if (index >= robbedPlayers.size()) {
            phaseCode = Events.DONE;
            return origPlayer;
        }
        phaseCode = Events.MORE_ACTIONS;
        updateMessage();
        return robbedPlayers.get(index);
    }

    @Override
    public int status() {
        return phaseCode;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public void updateButtons(MenuManager menus) {
        menus.hideAllButtons();
        if (currentPlayer().numSelectedCards() == numToDiscard())
            menus.setVisible("confirm", true);
    }

    @Override
    public void onScreenClick() {

    }

    @Override
    public void onFinish(Events events) {
        events.prepareToMoveRobber();
    }
}
