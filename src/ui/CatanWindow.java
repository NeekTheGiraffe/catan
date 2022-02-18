package ui;

import game.Catan;

import java.awt.*;
import javax.swing.*;

public class CatanWindow extends JFrame {

    public Catan game;

    public CatanWindow() {

        initUI();
    }

    private void initUI() {

        game = new Catan();
        add(game);

        setTitle("game.Catan");
        setSize(900,700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                CatanWindow window = new CatanWindow();
                window.setVisible(true);
            }
        });
    }
}
