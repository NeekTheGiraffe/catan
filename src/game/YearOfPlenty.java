package game;

import economy.Resource;
import ui.CButton;
import ui.Clickable;
import ui.PopUpWindow;
import ui.ResourceIcon;

import java.awt.*;

public class YearOfPlenty implements Event {

    private static final int NUM_RESOURCES = Resource.values().length;
    private static final int NUM_TO_COLLECT = 2;

    private PopUpWindow window;
    private Catan game;
    private Player actor;

    private ResourceIcon[][] icons = new ResourceIcon[NUM_TO_COLLECT][NUM_RESOURCES];
    private ResourceIcon[] selectedIcons = new ResourceIcon[NUM_TO_COLLECT];

    private boolean finished = false;

    public YearOfPlenty(Catan game) {
        this.game = game;
        this.actor = game.currentPlayer();
        initializeWindow();
    }

    @Override
    public boolean onClick(Clickable c) {

        if (c instanceof CButton b) {

            if (!b.getText().equals("confirm"))
                return false;

            // Player collects each resource from the stockpile
            for (int i = 0; i < NUM_TO_COLLECT; i++) {
                Resource r = selectedIcons[i].resource;

                if (actor.collect(r)) {
                    System.out.println(actor + " collects a " + r);
                } else {
                    System.out.println(actor + " tries to collect a " + r +
                            " but there is none left in the stockpile!");
                }
            }

            // Deselect all resource icons
            for (ResourceIcon[] iconArray : icons) {
                for (ResourceIcon icon : iconArray)
                    icon.setSelected(false);
            }

            finished = true;
            return true;
        }

        if (c instanceof ResourceIcon icon) {

            int row = rowOf(icon);
            if (icon.equals(selectedIcons[row]))
                return false;

            if (selectedIcons[row] != null)
                selectedIcons[row].setSelected(false);
            selectedIcons[row] = icon;
            icon.setSelected(true);
        }

        return false;
    }

    @Override
    public void onScreenClick() {

    }

    @Override
    public int status() {
        return (finished) ? Events.DONE : Events.MORE_ACTIONS;
    }

    @Override
    public Player nextPlayer() {
        return null;
    }

    @Override
    public Player currentPlayer() {
        return actor;
    }

    @Override
    public String message() {
        return "Click on two resources to collect them";
    }

    @Override
    public void updateButtons(MenuManager menus) {
        if (allRowsSelected())
            menus.show("confirm");
    }

    @Override
    public void onFinish(Events events) {
        game.getMenus().remove(window);
    }

    private void initializeWindow() {

        Dimension d = new Dimension(NUM_RESOURCES * 50 - 10, NUM_TO_COLLECT * 50 - 10);
        window = new PopUpWindow(game, d);

        for (int i = 0; i < NUM_TO_COLLECT; i++) {
            int j = 0;
            for (Resource res : Resource.values()) {

                int x = window.getX() + res.order() * 50;
                int y = window.getY() + i * 50;
                Rectangle rect = new Rectangle(x, y, 40, 40);
                ResourceIcon icon = new ResourceIcon(res, rect);
                window.add(icon);
                icons[i][j] = icon;
                j++;
            }
        }

        game.getMenus().add(window);
    }

    private int rowOf(ResourceIcon icon) {
        for (int i = 0; i < icons.length; i++) {
            for (int j = 0; j < icons[0].length; j++)
                if (icon.equals(icons[i][j]))
                    return i;
        }
        return -1;
    }

    private boolean allRowsSelected() {
        for (ResourceIcon selectedIcon : selectedIcons) {
            if (selectedIcon == null)
                return false;
        }
        return true;
    }
}
