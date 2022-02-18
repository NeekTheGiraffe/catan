package game;

import board.*;
import economy.Resource;
import economy.Stockpile;
import ui.Clickable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Catan extends JPanel implements MouseListener {

    public static final int DIE_SIDES = 6;
    public static final int NUM_DICE = 2;

    private final BufferedImage[] dieImgs = new BufferedImage[DIE_SIDES];

    private int[] dice = new int[NUM_DICE];

    private int diceSum;

    private int currentPlayer = 0;

    private final MenuManager menus;
    private final Board board;
    private final ArrayList<Player> players = new ArrayList<>();
    private final Stockpile stockpile = new Stockpile(players);

    private Events events = new Events(this);

    public Catan() {

        addMouseListener(this);
        loadImages();
        board = new Board(this);
        menus = new MenuManager(this);

        Arrays.fill(dice, 1);
        diceSum = dice.length;

        createPlayers();

        events.add(new SnakeDraft(players, 2, board, this));
        menus.setMessage(currentEvent().message());
        menus.update();
    }

    // Credit: Daniel Kvist on stackoverflow
    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    // Draws a string that is horizontally right-aligned and vertically centered
    public static void drawRightString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text));
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y;
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    public Event currentEvent() {
        return events.currentEvent();
    }

    public List<Player> getPlayers() {
        return players;
    }
    public int numPlayers() {
        return players.size();
    }

    public Board getBoard() {
        return board;
    }

    public Stockpile getStockpile() {
        return stockpile;
    }

    public MenuManager getMenus() {
        return menus;
    }

    public int numPlayersSelected() {
        int numSelected = 0;
        for (Player p : players) {
            if (p.isSelected())
                numSelected++;
        }
        return numSelected;
    }

    public static BufferedImage loadImage(String path) throws IOException {
        String newPath = "resources/" + path;
        return ImageIO.read(new File(newPath));
    }

    private void loadImages() {
        try {
            for (int i = 0; i < 6; i++) {
                dieImgs[i] = ImageIO.read(new File("resources/die" + (i+1) + ".png"));
            }
            System.out.println("Loaded all images in game.Catan.java");
        } catch (IOException e) {
            System.err.println("Failed to load images in game.Catan.java");
            e.printStackTrace();
        }
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create(); // Make a copy of the g object

        RenderingHints rh =
                new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

        rh.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setRenderingHints(rh);

        g2d.setBackground(new Color(18, 204, 255));
        g2d.clearRect(0,0,getWidth(),getHeight());

        // Draw dice
        for (int i = 0; i < dice.length; i++)
            g2d.drawImage(dieImgs[dice[i]-1], 20 + 70*i, 20, 50, 50, null);

        // Draw board
        board.draw(g2d);

        stockpile.draw(g2d);

        for (Player p : players) {
            //System.out.println("Drawing player " + p.getColor());
            p.draw(g2d);
        }

        menus.draw(g2d);

        g.dispose();
        //System.out.println("Width: " + getWidth() + ", Height: " + getHeight());
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }

    private void createPlayers() {
        addPlayer(Color.red, "RED");
        addPlayer(Color.green, "GREEN");
        addPlayer(Color.blue, "BLUE");
        addPlayer(Color.black, "BLACK");

        updateActivePlayer();
    }

    private void addPlayer(Color c, String name) {
        int nPlayers = players.size();
        players.add(new Player(c, name, stockpile, this, nPlayers));
    }

    // Removes all highlights.
    private void cleanBoard() {
        board.clearAllHighlights();
        currentPlayer().deselectAllCards();
        deselectAllPlayers();
    }

    public void deselectAllPlayers() {
        for (Player p : players)
            p.setSelected(false);
    }

    public Player currentPlayer() {
        return players.get(currentPlayer);
    }

    // Sets the next player as active, wrapping around to the 1st.
    // Returns true if we reached the last player and wrapped, false otherwise.
    private boolean nextPlayer() {
        boolean flag = false;
        currentPlayer++;
        if (currentPlayer == players.size()) {
            flag = true;
            currentPlayer = 0;
        }
        updateActivePlayer();
        return flag;
    }

    private void updateActivePlayer() {
        for (int i = 0; i < players.size(); i++) {
            boolean a = (i == currentPlayer);
            players.get(i).setActive(a);
        }
    }
    private void setActivePlayer(int turnOrder) {
        currentPlayer = turnOrder;
        updateActivePlayer();
    }
    private void setActivePlayer(Player p) {
        for (Player p1 : players) {
            p1.setActive(false);
        }
        p.setActive(true);
        currentPlayer = p.getTurnOrder();
    }

    public void nextPlayerTurn() {
        nextPlayer();
        events.add(new Turn(this, events));
    }

    private void rollDice() {

        diceSum = 0;
        for (int i = 0; i < dice.length; i++) {
            dice[i] = (int)(Math.random() * DIE_SIDES) + 1;
            diceSum += dice[i];
        }
        System.out.println("Dice rolled: " + diceSum);
    }

    // Gives resources to players based on current dice value.
    private void distributeResources() {

        // Initialize an empty HashMap. For each resource & player,
        // this map will say how much of that resource to give to that player.
        HashMap<Resource, int[]> resourcesToGive = new HashMap<>();
        for (Resource r : Resource.values()) {
            int[] zeroArray = new int[players.size()];
            for (int i = 0; i < players.size(); i++) {
                zeroArray[i] = 0;
            }
            resourcesToGive.put(r, zeroArray);
        }

        // Find each hex with the correct dice value.
        // For each neighboring settlement, prepare to give resources.
        for (Hex h : board.getHexes()) {
            if (h.hasRobber()) continue;
            if (h.getDiceValue() != diceSum) continue;
            for (Vertex vert : h.getVertexNeighbors()) {
                if (!vert.hasSettlement()) continue;
                for (int i = 0; i < players.size(); i++) {
                    if (players.get(i).equals(vert.getOwner())) {
                        if (h.getResource() == null) continue;
                        resourcesToGive.get(h.getResource())[i] +=
                                (vert.getLevel() == Settlement.CITY) ? 2 : 1;
                    }
                }
            }
        }

        // Now, for each resource, if there is enough resource in the pile,
        // each player collects appropriate resource.
        // If there is not enough, if there is one player, they collect as much as possible.
        // If there are multiple players, no one gets anything.
        for (Map.Entry<Resource, int[]> entry : resourcesToGive.entrySet()) {
            Resource r = entry.getKey();
            int[] values = entry.getValue();
            int sumResource = 0;
            int numPlayers = 0;
            int lastPlayer = -1;
            for (int i = 0; i < values.length; i++) {
                if (values[i] > 0) {
                    numPlayers++;
                    lastPlayer = i;
                }
                sumResource += values[i];
            }
            int stockLeft = stockpile.count(r);
            if (sumResource <= stockLeft) {
                for (int i = 0; i < values.length; i++) {
                    if (values[i] == 0) continue;
                    players.get(i).collect(r, values[i]);
                    System.out.println("Player " + players.get(i) + " collects "
                            + values[i] + " " + r);
                }
                continue;
            }
            System.out.println("There are " + sumResource + " of " +
                    r + " to be collected but only " + stockLeft +
                    " left in the stockpile");
            if (numPlayers != 1 || stockLeft == 0) {
                System.out.println("No one collects any " + r);
                continue;
            }
            System.out.println("Player " + players.get(lastPlayer) + " collects " + stockLeft + " " + r);
            players.get(lastPlayer).collect(r, values[lastPlayer]);
        }
    }

    // Gives players initial resources based on their first settlements.
    public void giveInitialResources() {
        for (Player p : players) {
            Settlement s = p.firstSettlement();
            ArrayList<Hex> neighbors = (ArrayList<Hex>) s.getVertex().getHexNeighbors();
            System.out.print("Player " + p + " collects ");
            for (Hex h : neighbors) {
                if (!h.hasResource()) continue;
                p.collect(h.getResource());
                System.out.print(h.getResource() + ", ");
            }
            System.out.println("from their first settlement");
        }
    }

    public void processEvent() {
        if (currentEvent() == null) {
            menus.clearMessage();
            return;
        }
        if (currentEvent().status() == Events.CHANGE_PLAYER) {
            setActivePlayer(currentEvent().nextPlayer());
        }
        if (currentEvent().status() == Events.DONE) {
            finishCurrentEvent();
        }
        if (currentEvent() != null)
            menus.setMessage(currentEvent().message());
    }

    public void finishCurrentEvent() {
        events.finishCurrentEvent();
        cleanBoard();
    }

    public void rollDiceAndGiveResources() {
        rollDice();
        if (diceSum == 7) {

            Robbery r = new Robbery(players, currentPlayer(), 8);
            if (r.status() == Events.DONE) {
                events.add(new MoveTheRobber(this));
            } else {
                events.add(r);
                setActivePlayer(r.currentPlayer());
                menus.setMessage(currentEvent().message());
            }
        } else {
            distributeResources();
        }
    }

    private Clickable getElementClickedOn(MouseEvent ev) {
        Clickable c;
        // First check if a button is clicked on
        c = menus.elementClicked(ev);
        if (c != null) {
            return c;
        }
        // Then check if the board was clicked on
        c = board.elementClickedOn(ev);
        if (c != null) {
            return c;
        }
        // Then check if a player was clicked on
        for (Player p : players)
            if (p.isInside(ev))
                return p;
        // Then check if a card was clicked on
        c = currentPlayer().getClickedCard(ev);
        if (c != null) {
            return c;
        }
        return null;
    }

    private void onMouseLeftClick(MouseEvent event) {
        //System.out.println("Mouse left clicked at " + e.getX() + ", " + e.getY());

        //System.out.println("The current player is " + currentPlayer());

        // Test for clickables (buttons & the game board)...
        Clickable c = getElementClickedOn(event);
        //System.out.println("You clicked on " + c);
        //System.out.println(events.numEvents() + ", " + currentEvent());
        if (currentEvent() != null) {
            if (c != null)
                currentEvent().onClick(c);
            else
                currentEvent().onScreenClick();
            processEvent();
        }
    }

    private void onMouseRightClick(MouseEvent e) {

    }
    @Override
    public void mouseClicked(MouseEvent e) {

    }
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            onMouseLeftClick(e);
            menus.update();
            repaint();
        }
        else if (e.getButton() == MouseEvent.BUTTON3) onMouseRightClick(e);
    }
    @Override
    public void mouseReleased(MouseEvent e) {

    }
    @Override
    public void mouseEntered(MouseEvent e) {

    }
    @Override
    public void mouseExited(MouseEvent e) {

    }
}