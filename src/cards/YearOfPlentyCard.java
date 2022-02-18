package cards;

import economy.HoldsCards;
import game.Catan;
import game.Event;
import game.YearOfPlenty;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static game.Catan.loadImage;

public class YearOfPlentyCard extends DevelopmentCard {

    private static BufferedImage image;

    static {
        try {
            image = loadImage("year_of_plenty.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Event event;

    public YearOfPlentyCard(HoldsCards owner) {
        super(owner);
    }

    @Override
    public Event getEvent(Catan game) {
        if (event == null)
            event = new YearOfPlenty(game);
        return event;
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Year of Plenty";
    }

    @Override
    public boolean playable() {
        return true;
    }
}
