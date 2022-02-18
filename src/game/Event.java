package game;

import ui.Clickable;

public interface Event {

    boolean onClick(Clickable c);
    void onScreenClick();
    int status();
    Player nextPlayer();
    Player currentPlayer();
    String message();
    void updateButtons(MenuManager menus);
    void onFinish(Events events);
}
