package game;

import java.util.ArrayList;

public class Events {

    private final Catan game;

    private ArrayList<Event> events;

    public static final int MORE_ACTIONS = 0;
    public static final int CHANGE_PLAYER = 1;
    public static final int DONE = 2;

    public Events(Catan game) {
        this.game = game;
        this.events = new ArrayList<>();
    }

    public Event currentEvent() {
        if (events.size() == 0) return null;
        return events.get(events.size() - 1);
    }

    public int numEvents() {
        return events.size();
    }

    public void add(Event e) {
        events.add(e);
    }

    public void finishCurrentEvent() {
        if (currentEvent().status() != DONE)
            throw new EventNotCompleteException(currentEvent() + " was not finished");

        int index = events.size() - 1;
        Event e = events.get(index);
        events.remove(index);
        e.onFinish(this);
    }

    public void prepareToMoveRobber() {
        events.add(new MoveTheRobber(game));
    }

    public static class EventNotCompleteException extends RuntimeException {
        public EventNotCompleteException(String msg) {
            super(msg);
        }
    }
}