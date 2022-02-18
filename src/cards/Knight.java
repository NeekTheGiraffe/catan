package cards;

import economy.HoldsCards;
import game.Catan;
import game.Event;
import game.MoveTheRobber;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static game.Catan.loadImage;

public class Knight extends DevelopmentCard {

    private static BufferedImage image;

    static {
        try {
            image = loadImage("knight.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Event event;

    public Knight(HoldsCards owner) {
        super(owner);
    }

    @Override
    public Event getEvent(Catan game) {
        if (event == null)
            event = new MoveTheRobber(game);
        return event;
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Knight";
    }

    @Override
    public boolean playable() {
        return true;
    }
}
