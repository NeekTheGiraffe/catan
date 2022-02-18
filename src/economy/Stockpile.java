package economy;

import cards.*;
import game.Catan;
import ui.Drawable;
import game.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Stockpile implements HoldsCards, Drawable {

    private List<DevelopmentCard> devCards = new ArrayList<>();
    private final List<Card> otherCards = new ArrayList<>();

    private final Set<ResourcePile> resourcePiles = new HashSet<>();

    private final LargestArmy largestArmy;
    private final LongestRoad longestRoad;

    public static final int NUM_EACH_RESOURCE = 19;

    public Stockpile(List<Player> players) {

        largestArmy = new LargestArmy(this, players);
        otherCards.add(largestArmy);
        longestRoad = new LongestRoad(this, players);
        otherCards.add(longestRoad);


        initializeStockpile();
        System.out.println("Created stockpile");
    }

    public LongestRoad getLongestRoad() {
        return longestRoad;
    }

    private class ResourcePile {

        Resource resource;
        int count;

        ResourcePile(Resource r) {
            resource = r;
            count = NUM_EACH_RESOURCE;
        }
    }

    public int numDevCards() {
        return devCards.size();
    }

    public DevelopmentCard topDevCard() {
        return devCards.get(devCards.size() - 1);
    }

    public LargestArmy getLargestArmy() {
        return largestArmy;
    }

    @Override
    public void add(Card c) {
        if (c instanceof ResourceCard) {
            getResourcePile(((ResourceCard) c).getResource()).count++;
        }
        else if (c instanceof DevelopmentCard)
            devCards.add((DevelopmentCard) c);
        else
            otherCards.add(c);
    }

    @Override
    public boolean remove(Card c) {
        if (c instanceof ResourceCard) {
            ResourcePile rp = getResourcePile(((ResourceCard) c).getResource());
            if (rp.count <= 0)
                return false;
            rp.count--;
            return true;

        } else if (c instanceof DevelopmentCard) {
            return devCards.remove(c);
        } else {
            return otherCards.remove(c);
        }
    }

    @Override
    public void add(Resource r, int count) {
        if (count <= 0) return;
        getResourcePile(r).count += count;
    }

    @Override
    public boolean remove(Resource r, int count) {
        if (count < 0) return false;
        ResourcePile rp = getResourcePile(r);
        if (rp.count >= count) {
            rp.count -= count;
            return true;
        }
        return false;
    }

    @Override
    public int count(Resource r) {
        ResourcePile rp = getResourcePile(r);
        if (rp == null) return 0;
        return rp.count;
    }

    //FIXME: Write "Stockpile" on top
    @Override
    public void draw(Graphics2D g2d) {

        // Draw resource piles.
        g2d.setStroke(new BasicStroke());
        g2d.setColor(Color.black);

        int baseX = 640;
        Font f = new Font("Purisa", Font.PLAIN, 18);

        for (ResourcePile rp : resourcePiles) {
            int x = baseX + rp.resource.order() * 50;

            g2d.drawRect(x, 10, 40, 40);
            g2d.drawImage(rp.resource.getImage(), x, 10, 40, 40, null);
            Catan.drawCenteredString(g2d, Integer.toString(rp.count), new Rectangle(x, 50, 40, 40), f);
        }

        // List # dev cards
        //g2d.setColor(Color.black);
        Rectangle r = new Rectangle(590, 10, 40, 40);
        g2d.draw(r);
        g2d.setColor(new Color(200,200,200));
        g2d.fill(r);
        g2d.setColor(Color.black);
        Catan.drawCenteredString(g2d, "D", r, f);
        r.translate(0, 40);
        Catan.drawCenteredString(g2d, Integer.toString(numDevCards()), r, f);

        // Draw largest army (ALSO DRAWS FOR PLAYERS)
        if (!largestArmy.getOwner().equals(this))
            largestArmy.draw(g2d);
        if (!longestRoad.getOwner().equals(this))
            longestRoad.draw(g2d);
    }

    private void initializeStockpile() {
        for (Resource r : Resource.values())
            resourcePiles.add(new ResourcePile(r));

        for (int i = 0; i < 14; i++)
            devCards.add(new Knight(this));
        for (int i = 0; i < 2; i++) {
            devCards.add(new RoadBuildingCard(this));
            devCards.add(new MonopolyCard(this));
            devCards.add(new YearOfPlentyCard(this));
        }
        for (int i = 0; i < 5; i++)
            devCards.add(new VictoryPointCard(this));


        shuffleDevCards();
    }

    private ResourcePile getResourcePile(Resource r) {

        for (ResourcePile rp : resourcePiles) {
            if (rp.resource == r)
                return rp;
        }
        return null;
    }

    private void shuffleDevCards() {
        ArrayList<DevelopmentCard> newList = new ArrayList<>();
        while (devCards.size() > 0) {
            int index = (int)(Math.random() * devCards.size());
            newList.add(devCards.get(index));
            devCards.remove(index);
        }
        devCards = newList;
    }
}
