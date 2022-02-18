package cards;

import economy.HoldsCards;
import game.Catan;
import game.Event;
import game.RoadBuilding;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static game.Catan.loadImage;

public class RoadBuildingCard extends DevelopmentCard {

    private static BufferedImage image;

    static {
        try {
            image = loadImage("road.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Event event;

    public RoadBuildingCard(HoldsCards owner) {
        super(owner);
    }

    @Override
    public Event getEvent(Catan game) {
        if (event == null)
            event = new RoadBuilding(game);
        return event;
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Road Building";
    }

    @Override
    public boolean playable() {
        return true;
    }
}
