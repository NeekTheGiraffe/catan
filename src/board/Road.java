package board;

import economy.Resource;
import game.Player;

import java.util.Map;
import java.util.HashMap;

public class Road {

    public final Edge edge;
    private Player owner;

    private static HashMap<Resource, Integer> PRICE;

    static {
        PRICE = new HashMap<>();
        PRICE.put(Resource.WOOD, 1);
        PRICE.put(Resource.BRICK, 1);
        System.out.println("Assigned price for roads");
    }

    public Road(Edge edge, Player owner) {
        this.edge = edge;
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public static Map<Resource, Integer> price() {
        return PRICE;
    }

    @Override
    public String toString() {
        return "[Road: owner=" + owner + ", edge=" + edge + "]";
    }
}
