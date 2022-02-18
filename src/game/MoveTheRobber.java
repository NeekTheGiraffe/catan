package game;

import board.Hex;
import cards.ResourceCard;
import economy.Robber;
import ui.CButton;
import ui.Clickable;

import java.util.ArrayList;

public class MoveTheRobber implements Event {

    private ArrayList<Player> neighboringPlayers;
    private final Player actor;
    private final Robber robber;
    private final Catan game;
    private Player victim;

    private boolean stolenYet = false;
    private boolean movedYet = false;

    public MoveTheRobber(Catan game) {

        this.game = game;
        this.actor = game.currentPlayer();
        this.robber = game.getBoard().getRobber();
    }

    // If a hex is clicked & we haven't moved yet, attempt to move the robber there.
    // If a player is clicked & we've moved, select them.
    @Override
    public boolean onClick(Clickable c) {
        if (c instanceof CButton) {
            CButton b = (CButton) c;
            if (b.getText().equals("confirm")) {
                steal();
                return true;
            }
            return false;
        }

        if (!movedYet && c instanceof Hex) {
            Hex h = (Hex) c;
            if (h.equals(robber.getHex())) return false; // Robber must change hex

            movedYet = true;
            robber.moveTo(h);
            System.out.println(actor + " moves robber to " + h);
            neighboringPlayers = (ArrayList<Player>) h.getPlayerNeighbors();
            neighboringPlayers.remove(actor);

            switch (neighboringPlayers.size()) {
                case 0:
                    System.out.println("There are no players to rob there");
                    stolenYet = true;
                    break;
                case 1:
                    System.out.println("There is only 1 player to rob, " + neighboringPlayers.get(0));
                    steal();
                    break;
                default:
                    System.out.println(actor + " must choose a player to rob");
                    break;
            }
            return true;
        }
        if (movedYet && c instanceof Player) {

            Player p = (Player) c;
            if (p.equals(victim)) return false;
            if (!neighboringPlayers.contains(p)) return false;
            victim = p;
            game.deselectAllPlayers();
            p.setSelected(true);
            return true;
        }
        return false;
    }

    // If the player has moved the robber and stolen from someone, we're done.
    @Override
    public int status() {
        return (movedYet && stolenYet) ? Events.DONE : Events.MORE_ACTIONS;
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
        if (movedYet)
            return "Click on a player to rob them";
        else
            return "Click on a hex to move the robber";
    }

    @Override
    public void updateButtons(MenuManager menus) {
        menus.hideAllButtons();
        if (victim != null)
            menus.setVisible("confirm", true);
    }

    private boolean steal() {
        if (victim == null)
            victim = neighboringPlayers.get(0);

        stolenYet = true;
        ResourceCard rc = victim.randomResourceCard();
        if (rc == null) {
            System.out.println(actor + " tries to steal from " + victim
                    + ", but they don't have any resources!");
            return false;
        }

        System.out.println(actor + " steals a random resource from " + victim);
        actor.take(rc, victim);
        return true;
    }

    @Override
    public void onScreenClick() {

    }

    @Override
    public void onFinish(Events events) {

    }
}
