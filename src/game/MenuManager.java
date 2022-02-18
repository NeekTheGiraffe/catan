package game;

import ui.Clickable;
import ui.Drawable;
import ui.CButton;
import ui.PopUpWindow;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MenuManager implements Drawable {

    private List<CButton> buttons = new ArrayList<>();
    private List<PopUpWindow> windows = new ArrayList<>();
    private final Catan game;

    private String msg = ""; // Tooltip message

    public MenuManager(Catan game) {

        this.game = game;
        initializeMenus();
        System.out.println("Initialized buttons");
        update();
    }

    private void initializeMenus() {
        Rectangle r;

        // End turn button
        r = new Rectangle(800, 615, 80, 35);
        add(new CButton(r, "end-turn", "End turn"));

        // Confirm button
        add(new CButton(r, "confirm","Confirm"));

        // Quick start button
        add(new CButton(r, "draft-quick-start", "Quick start"));

        // Buy button
        r = new Rectangle(700, 615, 80, 35);
        add(new CButton(r, "buy", "Buy"));

        // Cancel button
        add(new CButton(r, "cancel","Cancel"));

        // Buy road button
        r = new Rectangle(700, 570, 80, 35);
        add(new CButton(r, "road","Road"));

        // Buy settlement button
        r = new Rectangle(700, 525, 80, 35);
        add(new CButton(r, "settlement","Settlement"));

        // Buy city button
        r = new Rectangle(700, 480, 80, 35);
        add(new CButton(r, "city","City"));

        // Buy dev card button
        r = new Rectangle(700, 435, 80, 35);
        add(new CButton(r, "development-card","Dev card"));

        // Play dev card button
        r = new Rectangle(800, 570, 80, 35);
        add(new CButton(r, "play-card", "Play card"));

        // Trade button
        r = new Rectangle(600, 615, 80, 35);
        add(new CButton(r, "trade", "Trade"));
    }

    private void add(CButton b) {
        buttons.add(b);
    }

    public void add(PopUpWindow w) {
        windows.add(w);
    }
    public boolean remove(PopUpWindow w) {
        return windows.remove(w);
    }


    public void setMessage(String msg) {
        this.msg = msg;
    }
    public void clearMessage() {
        msg = "";
    }

    // Updates the visibility of all buttons
    public void update() {
        hideAllButtons();
        if (game.currentEvent() == null) return;
        game.currentEvent().updateButtons(this);
    }

    public Clickable elementClicked(MouseEvent ev) {
        for (PopUpWindow w : windows) {
            Clickable c = w.getElementClicked(ev);
            if (c != null)
                return c;
        }

        for (CButton b : buttons) {
            if (b.isInside(ev)) return b;
        }
        return null;
    }

    @Override
    public void draw(Graphics2D g2d) {

        // Draw the buttons
        for (CButton b : buttons) {
            b.draw(g2d);
        }

        // Draw the tooltip message
        g2d.setColor(Color.black);
        Font f = new Font("Purisa", Font.PLAIN, 18);
        g2d.setFont(f);
        FontMetrics metrics = g2d.getFontMetrics(f);
        g2d.drawString(msg, 150, 5 + metrics.getAscent());

        // Draw any pop-up windows
        for (PopUpWindow w : windows)
            w.draw(g2d);
    }


    private void show(CButton b) {
        b.setVisible(true);
    }
    private void hide(CButton b) {
        b.setVisible(false);
    }

    public void hideAllButtons() {
        for (CButton b : buttons)
            hide(b);
    }

    public CButton getButton(String text) {
        for (CButton button : buttons) {
            if (button.getText().equals(text))
                return button;
        }
        throw new IllegalArgumentException("text: " + text);
    }
    public void setVisible(String text, boolean visible) {
        CButton button = getButton(text);
        if (button == null) return;
        button.setVisible(visible);
    }
    public void show(String text) {
        setVisible(text, true);
    }
    public void hide(String text) {
        setVisible(text, false);
    }
}
