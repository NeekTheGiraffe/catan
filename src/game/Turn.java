package game;

import cards.DevelopmentCard;
import cards.Knight;
import economy.Resource;
import ui.CButton;
import ui.Clickable;

public class Turn implements Event {

    public static final int DICE = 0;
    public static final int MAIN = 1;
    public static final int DONE = 2;

    private int phase;
    private final Catan game;
    private final Events events;
    private final Player actor;

    private DevelopmentCard developmentCard;

    public Turn(Catan game, Events events) {
        this.game = game;
        this.events = events;
        this.actor = game.currentPlayer();

        actor.collect(Resource.WOOD, 7);
        actor.collect(Resource.BRICK, 7);
    }

    @Override
    public boolean onClick(Clickable c) {

        if (c instanceof CButton) {
            CButton b = (CButton) c;
            if (b.getText().equals("buy")) {
                events.add(new Purchase(game));
                return true;
            }
            if (b.getText().equals("end-turn")) {
                phase = DONE;
                return true;
            }
            if (b.getText().equals("play-card")) {

                System.out.println(actor + " plays " + developmentCard.getName());

                events.add(developmentCard.getEvent(game));
                if (developmentCard instanceof Knight)
                    game.currentPlayer().addKnightPlayed(); // Count knight for Largest Army

                game.currentPlayer().remove(developmentCard); // Remove the card from play
                developmentCard = null;
                return true;
            }
            if (b.getText().equals("trade")) {
                events.add(new Trade(game));
                return true;
            }
            return false;
        }

        if (c instanceof DevelopmentCard) {
            DevelopmentCard dc = (DevelopmentCard) c;

            if (!dc.playable())
                return false;

            if (dc.equals(developmentCard)) {
                developmentCard.setSelected(false);
                developmentCard = null;
            } else {
                if (developmentCard != null)
                    developmentCard.setSelected(false);
                developmentCard = dc;
                developmentCard.setSelected(true);
            }
            return true;
        }

        onScreenClick(); //FIXME: Make it so that clicking anywhere will roll dice if it's time to
        return false;
    }

    @Override
    public int status() {
        return (phase == DONE) ? Events.DONE : Events.MORE_ACTIONS;
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
        if (phase == DICE)
            return "Click anywhere to roll the dice";

        return "";
    }

    @Override
    public void updateButtons(MenuManager menus) {

        if (phase != MAIN) return;
        menus.show("buy");
        menus.show("end-turn");
        menus.show("trade");
        if (developmentCard != null)
            menus.show("play-card");
    }

    @Override
    public void onScreenClick() {
        //FIXME: Move dice roll logic into this class
        if (phase != DICE) return;
        game.rollDiceAndGiveResources();
        phase = MAIN;
    }

    @Override
    public void onFinish(Events events) {
        //FIXME: Start the turn of the next player.
        game.nextPlayerTurn();
    }
}
