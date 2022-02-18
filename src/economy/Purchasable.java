package economy;

import game.Player;

import java.util.Map;

public interface Purchasable {

    Map<Resource, Integer> price();

    void onPurchase(Player player);
}
