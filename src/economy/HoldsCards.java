package economy;

import cards.Card;

public interface HoldsCards {
    
    void add(Card c);
    boolean remove(Card c);
    
    void add(Resource r, int count);
    boolean remove(Resource r, int count);
    
    int count(Resource r);
    
    default boolean take(Card c, HoldsCards other) {
        if (other.remove(c)) {
            add(c);
            return true;
        }
        return false;
    }
    default boolean give(HoldsCards other, Card c) {
        return other.take(c, this);
    }
    
    default void add(Resource r) {
        add(r, 1);
    }
    default boolean remove(Resource r) {
        return remove(r, 1);
    }
    default boolean take(Resource r, int count, HoldsCards other) {
        if (other.remove(r, count)) {
            add(r, count);
            return true;
        }
        return false;
    }
    default boolean give(Resource r, int count, HoldsCards other) {
        return other.take(r, count, this);
    }
    default boolean take(Resource r, HoldsCards other) {
        return take(r, 1, other);
    }
    default boolean give(Resource r, HoldsCards other) {
        return give(r, 1, other);
    }

}