package cards;

import economy.HoldsCards;
import economy.Stockpile;
import game.Player;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static game.Catan.loadImage;

public class LargestArmy extends Card {

    public static final int THRESHOLD = 3;

    private static BufferedImage image;

    static {
        try {
            image = loadImage("largest-army.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final List<Player> players;
    private final Stockpile stockpile;

    public LargestArmy(Stockpile stockpile, List<Player> players) {
        super(stockpile);
        this.stockpile = stockpile;
        this.players = players;

        setPosition(400, 0);
    }

    public void findLargestArmy() {
        // Not owned by anyone yet
        if (getOwner().equals(stockpile)) {
            for (Player p : players) {
                if (p.armySize() >= THRESHOLD) {
                    changeOwner(p);
                    return;
                }
            }
        }
        // Owned by another player
        else {
            for (Player p : players) {
                if (p.armySize() > ((Player)getOwner()).armySize()) {
                    changeOwner(p);
                    return;
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Largest Army";
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
            setPosition(20,300);
        } else {
            setPosition(400, 0);
        }
    }
}