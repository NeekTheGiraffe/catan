package board;

import economy.Resource;
import game.Player;

import java.util.Map;
import java.util.HashMap;

public class Settlement {

    private Vertex vertex;
    private Player owner;
    private int level;

    public static final int SETTLEMENT = 0;
    public static final int CITY = 1;

    private static HashMap<Resource, Integer> PRICE;
    private static HashMap<Resource, Integer> CITY_PRICE;

    static {
        PRICE = new HashMap<>();
        PRICE.put(Resource.WOOD, 1);
        PRICE.put(Resource.BRICK, 1);
        PRICE.put(Resource.WHEAT, 1);
        PRICE.put(Resource.WOOL, 1);
        CITY_PRICE = new HashMap<>();
        CITY_PRICE.put(Resource.WHEAT, 2);
        CITY_PRICE.put(Resource.ORE, 3);
        System.out.println("Assigned prices for settlements and cities");
    }

    public Settlement(Vertex vertex, Player owner) {
        this.vertex = vertex;
        this.owner = owner;
        this.level = SETTLEMENT;
    }

    public Player getOwner() {
        return owner;
    }

    public int getLevel() {
        return level;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public boolean upgrade() {
        if (level == SETTLEMENT) {
            level = CITY;
            return true;
        }
        return false;
    }

    public static Map<Resource, Integer> price() {
        return PRICE;
    }
    public static Map<Resource, Integer> cityPrice() {
        return CITY_PRICE;
    }
}
