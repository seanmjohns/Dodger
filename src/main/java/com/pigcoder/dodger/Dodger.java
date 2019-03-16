package com.pigcoder.dodger;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Dodger {

    //The window
    public static JFrame screen;

    //The game area
    public static GameArea gameArea;

    //The player instance
    public static Player player;

    //All enemies
    public static ArrayList<Enemy> enemies = new ArrayList<Enemy>();

    //All keys
    public static ArrayList<String> keysHeld = new ArrayList<String>();

    //Screen dimensions
    public static Dimension size = new Dimension(400,400);

    //Whether or not the player has died
    public static boolean gameOver = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGui();
            }
        });
    }

    public static void createGui() {

        screen = new JFrame();

        //Set basic things
        screen.setTitle("Pigcoder's Dodger");
        screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set keybinds
        InputMap im = screen.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = screen.getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke("W"), "up");
        im.put(KeyStroke.getKeyStroke("A"), "left");
        im.put(KeyStroke.getKeyStroke("S"), "down");
        im.put(KeyStroke.getKeyStroke("D"), "right");

        am.put("up", new keyBinder("up"));
        am.put("left", new keyBinder("left"));
        am.put("down", new keyBinder("down"));
        am.put("right", new keyBinder("right"));

        im.put(KeyStroke.getKeyStroke("released W"), "upreleased");
        im.put(KeyStroke.getKeyStroke("released A"), "leftreleased");
        im.put(KeyStroke.getKeyStroke("released S"), "downreleased");
        im.put(KeyStroke.getKeyStroke("released D"), "rightreleased");

        am.put("upreleased", new keyBinder("releasedup"));
        am.put("leftreleased", new keyBinder("releasedleft"));
        am.put("downreleased", new keyBinder("releaseddown"));
        am.put("rightreleased", new keyBinder("releasedright"));

        //Create the gameArea
        gameArea = new GameArea();
        screen.add(gameArea);
        screen.getContentPane().setPreferredSize(size);
        screen.pack();

        //Create the executor for repainting the gameArea
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

        executor.scheduleAtFixedRate(new Repainter(gameArea), 0L, 50L, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(new EnemyCreator(), 0L, 200L, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(new InputManager(), 0L, 25L, TimeUnit.MILLISECONDS);


        screen.setVisible(true);

        startGame();

    }

    public static void startGame() {
        //Set the player's position
        player = new Player(new Point((int)size.getWidth()/2, (int)size.getHeight()/2));
    }

}

class GameArea extends JPanel {

    @Override
    public void paint(Graphics g) {

        Graphics2D graphicsSettings = (Graphics2D)g;

        //Draw the background
        graphicsSettings.setPaint(Color.WHITE);
        graphicsSettings.fillRect(0, 0, (int)Dodger.size.getWidth(), (int)Dodger.size.getHeight());

        //Draw the player
        graphicsSettings.setPaint(Color.GREEN);
        graphicsSettings.fillOval((int)Dodger.player.getPosition().getX(),(int)Dodger.player.getPosition().getY(),(int)Dodger.player.getSize().getWidth(),(int)Dodger.player.getSize().getHeight());

        //Move and Draw the enemies
        graphicsSettings.setPaint(Color.red);

        ArrayList<Enemy> enemies = new ArrayList<>(Dodger.enemies); //Copy the array here to prevent a ConcurrentModificationException

        for(Enemy e : enemies) {

            int x = (int)e.getPosition().getX();
            int y = (int)e.getPosition().getY();

            int width = (int)e.getSize().getWidth();
            int height = (int)e.getSize().getHeight();

            //Make sure it is on the screen
            graphicsSettings.fillRect(x, y, width, height);
        }
    }

}

class EnemyCreator implements Runnable {

    @Override
    public void run() {
        if(!Dodger.gameOver) {
            //Create a new enemy

            Random random = new Random();

            //A random size between the minimum size and maximum size, the enemy is always a square
            int size = random.nextInt(Enemy.sizeBoundaryUpper - Enemy.sizeBoundaryLower) + Enemy.sizeBoundaryLower;

            //A random speed (1 or 2)
            int speed = random.nextInt(2) + 1;

            Dodger.enemies.add(new Enemy(new Point(random.nextInt((int) Dodger.size.getWidth() - size), (int) Dodger.size.getHeight()), new Dimension(size, size), speed));

            //Remove any enemies that are not on the screen
            ArrayList<Enemy> updatedEnemies = new ArrayList<>();
            for (Enemy e : Dodger.enemies) {
                if (!(e.getPosition().getY() + e.getSize().getHeight() < 1)) {
                    updatedEnemies.add(e);
                }
            }
            Dodger.enemies = updatedEnemies;
        }
    }

}

class InputManager implements Runnable {

    public void run() {
        //player input
        if(!Dodger.gameOver && !Dodger.keysHeld.isEmpty()) {
            if (Dodger.keysHeld.contains("up")) {
                Dodger.player.move('w', Dodger.player.getSpeed());
            }
            if (Dodger.keysHeld.contains("left")) {
                Dodger.player.move('a', Dodger.player.getSpeed());
            }
            if (Dodger.keysHeld.contains("down")) {
                Dodger.player.move('s', Dodger.player.getSpeed());
            }
            if (Dodger.keysHeld.contains("right")) {
                Dodger.player.move('d', Dodger.player.getSpeed());
            }
        }
    }
}

class Repainter implements Runnable {

    private GameArea gameArea;

    public Repainter(GameArea gameArea) { this.gameArea = gameArea; }

    @Override
    public void run() {

        Dodger.size = Dodger.screen.getContentPane().getSize();

        //Update screen
        gameArea.repaint();

        //Check for collisions
        if (!Dodger.gameOver) {
            for (Enemy e : Dodger.enemies) {
                e.move();
                if (Dodger.player.getShape().intersects(e.getShape())) {
                    Dodger.gameOver = true;
                }
            }
        }
    }
}

class keyBinder extends AbstractAction {
    private String cmd;

    public keyBinder(String cmd){
        this.cmd = cmd;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //If a key was released, remove it from the keys held
        if(cmd.contains("released")) {
            Dodger.keysHeld.remove(cmd.substring(8));
        }
        //If a new key was pressed, add it
        else {
            if (!Dodger.keysHeld.contains(cmd)) {
                Dodger.keysHeld.add(cmd);
            }
        }
    }
}