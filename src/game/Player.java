package game;

import board.*;
import cards.*;
import economy.*;
import ui.Clickable;
import ui.Drawable;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class Player implements HoldsCards, Drawable, Clickable {

    private final Catan game;
    private final Stockpile stockpile;

    private final Color color;

    private boolean active;
    private final String name;
    private final int turnOrder;
    private boolean selected;

    private int numRoadsLeft = 15;
    private int numSettlementsLeft = 5;
    private int numCitiesLeft = 4;
    private int numKnightsPlayed = 0;
    private int victoryPoints = 0;
    private int longestRoad = 0;

    private List<ResourceCard> resourceCards = new ArrayList<>();
    private List<DevelopmentCard> devCards = new ArrayList<>();
    private List<Card> otherCards = new ArrayList<>();

    private Settlement firstSettlement;
    private Set<Settlement> settlements = new HashSet<>();
    private Set<Road> roads = new HashSet<>();

    private final Map<Resource, Integer> maritimePrice = new HashMap<>();

    public Player(Color color, String name, Stockpile stockpile, Catan game, int turnOrder) {
        this.stockpile = stockpile;
        this.color = color;
        this.active = false;
        this.name = name;
        this.game = game;
        this.turnOrder = turnOrder;
        this.selected = false;

        for (Resource res : Resource.values())
            maritimePrice.put(res, 4);
    }

    public Color getColor() {
        return color;
    }
    public Color getSecondaryColor() {
        return color.brighter();
    }
    public int getTurnOrder() {
        return turnOrder;
    }

    public int victoryPoints() {
        return victoryPoints;
    }

    public int countVictoryPoints() {
        int victoryPoints = 0;

        // Settlements
        for (Settlement s : settlements) {
            if (s.getLevel() == Settlement.CITY)
                victoryPoints += 2;
            else
                victoryPoints += 1;
        }
        // Victory point development cards
        victoryPoints += numVPCards();

        // Largest army
        if (stockpile.getLargestArmy().getOwner().equals(this))
            victoryPoints += 2;
        if (stockpile.getLongestRoad().getOwner().equals(this))
            victoryPoints += 2;

        this.victoryPoints = victoryPoints;
        return victoryPoints;
    }

    public boolean buildRoad(Edge e) {
        if (numRoadsLeft == 0) return false;

        Road nr = new Road(e, this);
        if (!e.setRoad(nr)) return false;
        numRoadsLeft--;
        roads.add(nr);

        // Handle Longest road
        for (Player p : game.getPlayers()) {
            p.calculateLongestRoad();
        }

        LongestRoad longestRoad = stockpile.getLongestRoad();
        HoldsCards prevOwner = longestRoad.getOwner();
        longestRoad.findLongestRoad(); // Automatically changes owner if necessary

        if (prevOwner.equals(longestRoad.getOwner()))
            return true;

        for (Player p : game.getPlayers())
            p.countVictoryPoints();

        return true;
    }
    public boolean settle(Vertex v) {
        if (numSettlementsLeft == 0) return false;

        Settlement ns = new Settlement(v, this); // New settlement
        if (!v.setSettlement(ns)) return false;
        numSettlementsLeft--;
        settlements.add(ns);
        countVictoryPoints();

        if (settlements.size() == 1)
            firstSettlement = ns;

        // Update maritime price
        for (Edge e : v.getEdgeNeighbors()) {
            Harbor h = e.getHarbor();
            if (h == null)
                continue;

            if (h.getResource() == null) {
                for (Map.Entry<Resource, Integer> entry : maritimePrice.entrySet()) {
                    if (entry.getValue() == 4)
                        entry.setValue(3);
                }
            } else {
                maritimePrice.replace(h.getResource(), 2);
            }
        }

        return true;
    }
    public boolean upgrade(Vertex v) {
        if (numCitiesLeft == 0) return false;
        if (!v.hasSettlement()) return false;
        if (!v.getSettlement().getOwner().equals(this)) return false;
        if (!v.getSettlement().upgrade()) return false;
        numCitiesLeft--;
        numSettlementsLeft++;
        countVictoryPoints();
        return true;
    }

    public Settlement firstSettlement() {
        return firstSettlement;
    }

    public void setActive(boolean a) {
        active = a;
    }
    public boolean isActive() {
        return active;
    }

    public int numSettlements() {
        return settlements.size();
    }

    public List<Vertex> getSettleableVertices() {
        List<Vertex> vertices = new ArrayList<>();
        for (Road r : roads) {
            // Check the first vertex
            Vertex v1 = r.edge.getUpVertex();
            if (v1.isOpen() && !vertices.contains(v1))
                vertices.add(v1);
            // Check the second vertex
            Vertex v2 = r.edge.getDownVertex();
            if (v2.isOpen() && !vertices.contains(v2))
                vertices.add(v2);
        }
        return vertices;
    }
    public boolean canSettle(Vertex v) {
        return getSettleableVertices().contains(v);
    }
    public List<Edge> getBuildableEdges() {
        List<Edge> edges = new ArrayList<>();

        // For each road, check both neighboring vertices
        for (Road r : roads) {
            for (Vertex.VertexType vtype : Vertex.VertexType.values()) {
                Vertex v1 = r.edge.getVertex(vtype);
                Player v1Owner = v1.getOwner();

                // We can't build through this vertex if it is owned by someone else.
                if (v1Owner != null && !this.equals(v1.getOwner())) continue;

                for (Edge e : r.edge.getVertexEdgeNeighbors(vtype)) {
                    if (e.hasRoad()) continue;
                    if (edges.contains(e)) continue;
                    edges.add(e);
                }
            }
        }
        return edges;
    }
    public boolean canBuildOn(Edge e) {
        return getBuildableEdges().contains(e);
    }
    public List<Vertex> getUpgradableVertices() {
        ArrayList<Vertex> vertices = new ArrayList<>();
        for (Settlement s : settlements) {
            if (s.getLevel() == Settlement.SETTLEMENT)
                vertices.add(s.getVertex());
        }
        return vertices;
    }
    public boolean canUpgrade(Vertex v) {
        return getUpgradableVertices().contains(v);
    }

    public int numResources() {
        return resourceCards.size();
    }

    public ResourceCard randomResourceCard() {
        if (resourceCards.size() == 0) return null;
        int i = (int)(Math.random() * resourceCards.size());
        return resourceCards.get(i);
    }

    public Card getClickedCard(MouseEvent e) {
        for (ResourceCard rc : resourceCards)
            if (rc.isInside(e))
                return rc;
        for (DevelopmentCard dc : devCards)
            if (dc.isInside(e))
                return dc;
        return null;
    }
    public void deselectAllCards() {
        for (ResourceCard rc : resourceCards)
            rc.setSelected(false);
    }
    public int numSelectedCards() {
        int numSelected = 0;
        for (ResourceCard rc : resourceCards) {
            if (rc.isSelected())
                numSelected++;
        }
        return numSelected;
    }

    public int maritimePrice(Resource res) {
        return maritimePrice.get(res);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void addKnightPlayed() {
        numKnightsPlayed++;

        LargestArmy largestArmy = stockpile.getLargestArmy();
        HoldsCards prevOwner = largestArmy.getOwner();
        largestArmy.findLargestArmy(); // Automatically changes owner if necessary

        if (prevOwner.equals(largestArmy.getOwner()))
            return;

        for (Player p : game.getPlayers())
            p.countVictoryPoints();
    }

    public int armySize() {
        return numKnightsPlayed;
    }

    public int longestRoad() {
        return longestRoad;
    }
    public void calculateLongestRoad() {

        int longest = 0;
        for (Road origRoad : roads) {

            Set<Road> path = new HashSet<>();
            path.add(origRoad);
            int size = longestRoadStartingWith(path, origRoad, null, origRoad).size();
            if (size > longest)
                longest = size;
        }
        longestRoad = longest;
    }
    private Set<Road> longestRoadStartingWith(Set<Road> path, Road leadRoad, Road prevRoad, Road origRoad) {

        Set<Road> longest = path;
        Set<Road> nextRoads = nextRoads(path, leadRoad, prevRoad);

        if (nextRoads.size() == 0) {
            // No more roads
            // The path must connect 2 DIFFERENT vertices
            //System.out.print(this + ":" + longest.size() + "-->");
            adjustForSameEndpoint(longest, leadRoad, prevRoad, origRoad);
            //System.out.println(longest.size());
        }

        for (Road nextRoad : nextRoads) {
            Set<Road> altPath = new HashSet<>(path);
            altPath.add(nextRoad);

            Set<Road> candidate = longestRoadStartingWith(altPath, nextRoad, leadRoad, origRoad);
            if (candidate.size() > longest.size())
                longest = candidate;
        }
        return longest;
    }

    private void adjustForSameEndpoint(Set<Road> path, Road leadRoad, Road prevRoad, Road origRoad) {
        if (prevRoad == null)
            return;

        //System.out.println("Testing. leadRoad=" + leadRoad + ", prevRoad=" + prevRoad + ", origRoad=" + origRoad);

        // If prevRoad shares an up vertex with leadRoad,
        // then the "endpoint" of leadRoad is the down vertex
        boolean b1 = prevRoad.edge.getUpVertex().equals(leadRoad.edge.getUpVertex());
        boolean b2 = leadRoad.edge.getDownVertex().equals(origRoad.edge.getDownVertex());
        boolean b3 = prevRoad.edge.getDownVertex().equals(leadRoad.edge.getDownVertex());
        boolean b4 = leadRoad.edge.getUpVertex().equals(origRoad.edge.getUpVertex());
        //System.out.println("b1=" + b1 + ", b2=" + b2 + ", b3=" + b3 + ", b4=" + b4);

        if ((b1 && b2) || (b3 && b4)) {
            System.out.print("Removing-->");
            path.remove(leadRoad);
            //System.out.println(path);
        }
    }
    private Set<Road> nextRoads(Set<Road> path, Road leadRoad, Road prevRoad) {

        Set<Road> upVertexResults = checkUpVertex(path, leadRoad);
        Set<Road> downVertexResults = checkDownVertex(path, leadRoad);

        if (prevRoad != null) {
            if (prevRoad.edge.getUpVertex().equals(leadRoad.edge.getUpVertex()))
                return downVertexResults;
            if (prevRoad.edge.getDownVertex().equals(leadRoad.edge.getDownVertex()))
                return upVertexResults;
        }

        upVertexResults.addAll(downVertexResults);
        return upVertexResults;
    }
    private Set<Road> checkVertex(Set<Road> path, Road leadRoad, Vertex v) {
        Set<Road> roads = new HashSet<>();
        Player owner = v.getOwner();

        // Roads aren't "connected" if they are blocked by another player
        if (owner != null && !this.equals(v.getOwner())) return roads;

        Set<Edge> roadCandidates = new HashSet<>(v.getEdgeNeighbors());
        roadCandidates.remove(leadRoad.edge);
        for (Edge e : roadCandidates) {

            if (!e.hasRoad()) continue;
            if (!e.getRoad().getOwner().equals(this)) continue;
            if (path.contains(e.getRoad())) continue;
            roads.add(e.getRoad());
        }
        return roads;
    }
    private Set<Road> checkUpVertex(Set<Road> path, Road leadRoad) {
        return checkVertex(path, leadRoad, leadRoad.edge.getUpVertex());
    }
    private Set<Road> checkDownVertex(Set<Road> path, Road leadRoad) {
        return checkVertex(path, leadRoad, leadRoad.edge.getDownVertex());
    }


    public Point getPosition() {
        int x = 10;
        int y = 600;
        if (!active) {
            x = 840;
            y = 100;
            int turnOrderDifference = turnOrder - game.currentPlayer().getTurnOrder();
            if (turnOrderDifference > 0) {
                y += (turnOrderDifference - 1) * 50;
            } else {
                y += (game.numPlayers() + turnOrderDifference - 1) * 50;
            }
        }
        return new Point(x, y);
    }
    public int getX() {
        return getPosition().x;
    }
    public int getY() {
        return getPosition().y;
    }
    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean isInside(MouseEvent e) {
        int x = getX();
        int y = getY();
        int squareSize = 40;
        if (active)
            squareSize = 50;
        return (e.getX() >= x && e.getX() <= x + squareSize
                && e.getY() >= y && e.getY() <= y + squareSize);
    }

    //FIXME: Finish draw
    @Override
    public void draw(Graphics2D g2d) {
        int x = getX();
        int y = getY();
        if (active) {
            g2d.setStroke(new BasicStroke());
            Rectangle r = new Rectangle(x,y,40,40);
            g2d.setColor(color);
            g2d.fill(r);
            g2d.setColor(Color.black);
            g2d.draw(r);

            // Write how many settlements/cities/roads left
            g2d.setColor(Color.black);
            Font f = new Font("Purisa", Font.PLAIN, 16);
            g2d.setFont(f);
            FontMetrics metrics = g2d.getFontMetrics(f);
            y = 520;
            g2d.drawString("Roads: " + numRoadsLeft, 10, y);
            y -= metrics.getAscent() + 5;
            g2d.drawString("Cities: " + numCitiesLeft, 10, y);
            y -= metrics.getAscent() + 5;
            g2d.drawString("Settlements: " + numSettlementsLeft, 10, y);
            y -= metrics.getAscent() + 5;
            g2d.drawString("Army: " + numKnightsPlayed, 10, y);
            y -= metrics.getAscent() + 5;
            g2d.drawString("Longest road: " + longestRoad, 10, y);
            y -= metrics.getAscent() + 5;
            g2d.drawString("Victory Points: " + victoryPoints, 10, y);

            for (ResourceCard rc : resourceCards) {
                //System.out.println("Drawing: " + rc + " at " + rc.getX() + ", " + rc.getY());
                rc.draw(g2d);
            }
            for (DevelopmentCard dc : devCards)
                dc.draw(g2d);

        } else {
            Rectangle r = new Rectangle(x, y, 40, 40);
            if (selected) {
                g2d.setStroke(new BasicStroke(3));
                g2d.setColor(Color.yellow);
            } else {
                g2d.setStroke(new BasicStroke());
                g2d.setColor(Color.black);
            }
            g2d.draw(r);
            g2d.setColor(color);
            g2d.fill(r);
        }
    }

    public boolean collect(Resource r) {
        return collect(r, 1);
    }
    public boolean collect(Resource r, int count) {
        return take(r, count, stockpile);
    }

    public boolean discard(Resource r, int count) {
        return give(r, count, stockpile);
    }
    public void discardSelectedCards() {
        for (int i = 0; i < resourceCards.size(); i++) {
            ResourceCard rc = resourceCards.get(i);
            if (rc.isSelected()) {
                give(stockpile, rc);
                i--;
            }
        }
    }

    public boolean canAfford(Map<Resource, Integer> price) {
        for (Map.Entry<Resource, Integer> entry : price.entrySet()) {
            if (count(entry.getKey()) < entry.getValue()) return false;
        }
        return true;
    }
    public void pay(Map<Resource, Integer> price) {
        if (!canAfford(price))
            throw new IllegalStateException("Cannot afford the price");

        for (Map.Entry<Resource, Integer> entry : price.entrySet()) {
            if (!discard(entry.getKey(), entry.getValue()))
                System.err.println("Error: expected to be able to pay price but couldn't");
        }
    }

    public void takeDevCard() {
        take(stockpile.topDevCard(), stockpile);
        shiftHand();
    }

    // Implemented methods
    @Override
    public String toString() {
        return name;
    }

    @Override
    public void add(Card c) {
        if (c instanceof ResourceCard) {
            resourceCards.add((ResourceCard) c);
            sortHand();
        }
        else if (c instanceof DevelopmentCard) {
            devCards.add((DevelopmentCard) c);
            if (c instanceof VictoryPointCard)
                countVictoryPoints();
        } else
            otherCards.add(c);
        c.setOwner(this);
    }

    @Override
    public boolean remove(Card c) {
        if (c instanceof ResourceCard) {
            if (resourceCards.remove(c)) {
                shiftHand();
                return true;
            }
            return false;
        } else if (c instanceof DevelopmentCard) {
            if (devCards.remove(c)) {
                shiftHand();
                return true;
            }
            return false;
        }
        return otherCards.remove(c);
    }

    @Override
    public void add(Resource r, int count) {
        for (int i = 0; i < count; i++)
            resourceCards.add(new ResourceCard(this, r));
        sortHand();
    }

    // Attempts to remove count cards of the specified resource
    // If there exist count or more cards, removes them & returns true
    // If there aren't enough cards, returns false and leaves hand unchanged.
    @Override
    public boolean remove(Resource r, int count) {
        if (count < 0) return false;
        if (count == 0) return true;

        int numCards = 0;
        ResourceCard[] cards = new ResourceCard[count];

        for (ResourceCard rc : resourceCards) {
            if (rc.getResource() != r) continue;

            cards[numCards] = rc;
            numCards++;

            if (numCards == count) {
                for (int j = 0; j < count; j++)
                    resourceCards.remove(cards[j]);
                shiftHand();
                return true;
            }
        }
        return false;
    }

    @Override
    public int count(Resource r) {
        int count = 0;
        for (ResourceCard rc : resourceCards) {
            if (rc.getResource() == r)
                count++;
        }
        return count;
    }

    private int numVPCards() {
        int count = 0;
        for (DevelopmentCard dc : devCards) {
            if (dc instanceof VictoryPointCard)
                count++;
        }
        return count;
    }

    // Sorts the hand in alphabetical order
    // and shifts the x,y positions.
    private void sortHand() {
        mergeSort(resourceCards);
        shiftHand();
    }
    // Updates the x,y positions of each card without
    // sorting the hand.
    private void shiftHand() {
        int x = 60;
        int y = 600;
        for (ResourceCard rc : resourceCards) {
            rc.setPosition(x, y);
            //System.out.println("Set the position of " + rc + " to " + x + ", " + y);
            x += 40;
        }

        x = 60;
        y = 530;
        for (DevelopmentCard dc : devCards) {
            dc.setPosition(x, y);
            x += 40;
        }
    }

    //FIXME: increase type flexibility.
    private void mergeSort(List<ResourceCard> cards) {
        mergeSort(cards, 0, cards.size());
    }
    // End is exclusive.
    private void mergeSort(List<ResourceCard> cards, int start, int end) {
        if (end - start <= 1) return;
        int middle = start + ((end-start) + 1) / 2;
        mergeSort(cards, start, middle);
        mergeSort(cards, middle, end);
        merge(cards, start, middle, end);
    }
    private void merge(List<ResourceCard> a, int start, int middle, int end) {

        List<ResourceCard> left = new ArrayList<>();
        List<ResourceCard> right = new ArrayList<>();
        for (int i = start; i < middle; i++)
            left.add(a.get(i));
        for (int i = middle; i < end; i++)
            right.add(a.get(i));

        int i1 = 0;
        int i2 = 0;
        int s1 = middle - start;
        int s2 = end - middle;
        int k = start;
        while (i1 < s1 && i2 < s2) {
            if (left.get(i1).getResource().order() < right.get(i2).getResource().order()) {
                a.set(k, left.get(i1));
                i1++;
            } else {
                a.set(k, right.get(i2));
                i2++;
            }
            k++;
        }
        while (i1 < s1) {
            a.set(k, left.get(i1));
            i1++;
            k++;
        }
        while (i2 < s2) {
            a.set(k, right.get(i2));
            i2++;
            k++;
        }
    }
}
