package game;

import economy.Resource;
import ui.CButton;
import ui.Clickable;
import ui.PopUpWindow;
import ui.ResourceIcon;

import java.awt.*;

public class Monopoly implements Event {

    private static final int NUM_RESOURCES = Resource.values().length;

    private PopUpWindow window;
    private Catan game;
    private Player actor;

    private ResourceIcon[] icons = new ResourceIcon[NUM_RESOURCES];
    private ResourceIcon icon;

    private boolean finished = false;

    public Monopoly(Catan game) {
        this.game = game;
        this.actor = game.currentPlayer();
        initializeWindow();
    }

    @Override
    public boolean onClick(Clickable c) {

        if (c instanceof CButton) {
            CButton b = (CButton) c;
            if (!b.getText().equals("confirm"))
                return false;

            for (ResourceIcon i : icons)
                i.setSelected(false);

            for (Player p : game.getPlayers()) {
                if (p.equals(actor))
                    continue;

                Resource r = icon.resource;
                int count = p.count(r);
                System.out.println(p + " gives all " + count + " of their " + r + " to " + actor);
                p.give(r, count, actor);
            }

            finished = true;
            return true;
        }

        if (c instanceof ResourceIcon) {
            ResourceIcon i = (ResourceIcon) c;

            if (i.equals(icon))
                return false;

            if (icon != null)
                icon.setSelected(false);
            icon = i;
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
        return "Click on a resource to claim all of it";
    }

    @Override
    public void updateButtons(MenuManager menus) {
        if (icon != null)
            menus.show("confirm");
    }

    @Override
    public void onFinish(Events events) {
        game.getMenus().remove(window);
    }

    private void initializeWindow() {

        Dimension d = new Dimension(NUM_RESOURCES * 50 - 10, 40);
        window = new PopUpWindow(game, d);
        int i = 0;
        for (Resource res : Resource.values()) {

            int x = window.getX() + res.order() * 50;
            int y = window.getY();
            Rectangle rect = new Rectangle(x, y, 40, 40);
            ResourceIcon icon = new ResourceIcon(res, rect);
            window.add(icon);
            icons[i] = icon;
            i++;
        }
        game.getMenus().add(window);
    }
}
