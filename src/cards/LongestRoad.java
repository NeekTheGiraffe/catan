package cards;

import economy.HoldsCards;
import economy.Stockpile;
import game.Player;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static game.Catan.loadImage;

public class LongestRoad extends Card {

    public static final int THRESHOLD = 5;

    private static BufferedImage image;

    static {
        try {
            image = loadImage("longest-road.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final List<Player> players;
    private final Stockpile stockpile;

    public LongestRoad(Stockpile stockpile, List<Player> players) {
        super(stockpile);
        this.stockpile = stockpile;
        this.players = players;

        setPosition(350, 0);
    }

    public void findLongestRoad() {
        // Not owned by anyone yet
        if (getOwner().equals(stockpile)) {
            for (Player p : players) {
                if (p.longestRoad() >= THRESHOLD) {
                    changeOwner(p);
                    return;
                }
            }
        }

        // Owned by another player
        else {

            int longest = players.get(0).longestRoad();
            List<Player> longestRoads = new ArrayList<>();
            Player currOwner = (Player)getOwner();

            for (Player p : players) {
                int length = p.longestRoad();
                if (length == longest)
                    longestRoads.add(p);
                else if (length > longest) {
                    longestRoads.clear();
                    longestRoads.add(p);
                    longest = length;
                }
            }

            if (currOwner.longestRoad() == longest)
                return;
            if (longestRoads.size() > 1 || longest < THRESHOLD) {
                changeOwner(stockpile);
                return;
            }
            changeOwner(longestRoads.get(0));
        }
    }

    @Override
    public String getName() {
        return "Longest Road";
    }

    @Override
    public void draw(Graphics2D g2d) {

        Rectangle r = new Rectangle(getX(), getY(), Card.WIDTH, Card.HEIGHT);
        g2d.draw(r);
        g2d.setColor(Color.white);
        g2d.fill(r);
        g2d.drawImage(image, getX()+1, getY()+13, 35, 35, null);
    }

    @Override
    public boolean isInside(MouseEvent e) {
        return false;
    }

    private void changeOwner(HoldsCards newOwner) {
        HoldsCards prevOwner = getOwner();
        newOwner.take(this, prevOwner);

        if (newOwner instanceof Player) {
            setPosition(70,300);
        } else {
            setPosition(350, 0);
        }
    }
}
