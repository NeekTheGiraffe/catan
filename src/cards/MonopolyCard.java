package cards;

import economy.HoldsCards;
import game.Catan;
import game.Event;
import game.Monopoly;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static game.Catan.loadImage;

public class MonopolyCard extends DevelopmentCard {

    private static BufferedImage image;

    static {
        try {
            image = loadImage("monopoly.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Event event;

    public MonopolyCard(HoldsCards owner) {
        super(owner);
    }

    @Override
    public Event getEvent(Catan game) {
        if (event == null)
            event = new Monopoly(game);
        return event;
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Monopoly";
    }

    @Override
    public boolean playable() {
        return true;
    }
}
