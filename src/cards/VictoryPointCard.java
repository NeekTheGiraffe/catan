package cards;

import economy.HoldsCards;
import game.Catan;
import game.Event;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static game.Catan.loadImage;

public class VictoryPointCard extends DevelopmentCard {

    private static BufferedImage image;

    static {
        try {
            image = loadImage("coin.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VictoryPointCard(HoldsCards owner) {
        super(owner);
    }

    @Override
    public Event getEvent(Catan game) {
        return null;
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Victory Point";
    }

    @Override
    public boolean playable() {
        return false;
    }
}
