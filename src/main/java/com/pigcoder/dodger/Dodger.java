package com.pigcoder.dodger;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.Random;

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

    //Whether or not the game is paused
    public static boolean gamePaused = false;

    //Create enemies every 100 milliseconds
    public static Timer enemyCreator = new Timer(100, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!gameOver) {
                //Create a new enemy

                Random random = new Random();

                //A random size between the minimum size and maximum size, the enemy is always a square
                int size = random.nextInt(Enemy.sizeBoundaryUpper - Enemy.sizeBoundaryLower) + Enemy.sizeBoundaryLower;

                //A random speed (1 or 2)
                double speed = Math.random()*1.5 + 0.5;

                enemies.add(new Enemy(random.nextInt((int)Dodger.size.getWidth() - size), (int)Dodger.size.getHeight(), new Dimension(size, size), speed));

                //Remove any enemies that are not on the screen
                ArrayList<Enemy> updatedEnemies = new ArrayList<>();
                for (Enemy enemy : enemies) {
                    if (!(enemy.y + enemy.height < 1)) {
                        updatedEnemies.add(enemy);
                    }
                }
                enemies = updatedEnemies;
            }
        }
    });

    //Move the enemies every 25 milliseconds.
    public static Timer enemyMover = new Timer(25, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //Move the enemies and check for collisions
            if (!gameOver) {
                for (Enemy enemy : enemies) {
                    enemy.move();
                    if (player.intersects(enemy)) {
                        gameOver = true;
                    }
                }
            }
        }
    });

    //Get player input every 20 milliseconds
    public static Timer inputManager = new Timer(20, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //Move the player
            if(!gameOver && !gamePaused) {
                //Vertical movement
                if(keysHeld.contains("up")) {
                    player.decreaseyVel(0.15);
                }
                else if(keysHeld.contains("down")) {
                    player.increaseyVel(0.15);
                }
                else {//Slow the player down if no keys are being pressed
                    player.decreaseyVel(player.getyVel() / 50); //Halves speed
                }

                //Horizontal movement
                if(keysHeld.contains("left")) {
                    player.decreasexVel(0.15);
                }
                else if(keysHeld.contains("right")) {
                    player.increasexVel(0.15);
                }
                else { //Slow the player down if no keys are being pressed
                    player.decreasexVel(player.getxVel() / 50); //Halves speed
                }

                //If the player has run into a wall, stop them
                if(player.x + player.getxVel() <= 0) {
                    player.x = 0;
                    player.setxVel(0);
                }
                if(player.x + player.getSize().getWidth() + player.getxVel() >= size.getWidth()) {
                    player.x = size.getWidth() -  player.getSize().getWidth();
                    player.setxVel(0);
                }
                if(player.y + player.getyVel() <= 0) {
                    player.y = 0;
                    player.setyVel(0);
                }
                if(player.y + player.getSize().getWidth() + player.getyVel() >= size.getHeight()) {
                    player.y = size.getHeight() - player.getSize().getWidth();
                    player.setyVel(0);
                }
                //Move the player according to the velocity
                player.x+=player.getxVel();
                player.y+=player.getyVel();
            }
        }
    });

    //Repaint every 16 milliseconds (About 60 fps)
    public static Timer repainter = new Timer(16, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            gameArea.repaint();
        }
    });

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGui();
            }
        });
    }

    public static void createGui() {

        screen = new JFrame();
        screen.setResizable(false);

        //Set basic things
        screen.setTitle("Pigcoder's Dodger");
        screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set keybinds
        InputMap im = screen.getRootPane().getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = screen.getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke("W"), "up");
        im.put(KeyStroke.getKeyStroke("A"), "left");
        im.put(KeyStroke.getKeyStroke("S"), "down");
        im.put(KeyStroke.getKeyStroke("D"), "right");
        im.put(KeyStroke.getKeyStroke("R"), "restart");
        im.put(KeyStroke.getKeyStroke("F"), "pause");

        am.put("up", new KeyBinder("up"));
        am.put("left", new KeyBinder("left"));
        am.put("down", new KeyBinder("down"));
        am.put("right", new KeyBinder("right"));
        am.put("restart", new KeyBinder("restart"));
        am.put("pause", new KeyBinder("pause"));

        //Released keybinder
        im.put(KeyStroke.getKeyStroke("released W"), "upreleased");
        im.put(KeyStroke.getKeyStroke("released A"), "leftreleased");
        im.put(KeyStroke.getKeyStroke("released S"), "downreleased");
        im.put(KeyStroke.getKeyStroke("released D"), "rightreleased");

        am.put("upreleased", new KeyBinder("releasedup"));
        am.put("leftreleased", new KeyBinder("releasedleft"));
        am.put("downreleased", new KeyBinder("releaseddown"));
        am.put("rightreleased", new KeyBinder("releasedright"));

        //Create the gameArea
        gameArea = new GameArea();
        screen.add(gameArea);
        screen.getContentPane().setPreferredSize(size);
        screen.pack();

        repainter.start();
        inputManager.start();
        enemyCreator.start();
        enemyMover.start();

        screen.setVisible(true);

        startGame();

    }

    public static void startGame() {
        //Set the player's position
        gameOver = false;
        unpause();
        enemies.clear();
        player = new Player((int)size.getWidth()/2, (int)size.getHeight()/2);
    }

    public static void pause() {
        Dodger.enemyCreator.stop();
        Dodger.enemyMover.stop();
        Dodger.inputManager.stop();
        Dodger.gamePaused = true;
    }

    public static void unpause() {
        Dodger.enemyCreator.start();
        Dodger.enemyMover.start();
        Dodger.inputManager.start();
        Dodger.gamePaused = false;
    }

}

class GameArea extends JPanel {

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D graphicsSettings = (Graphics2D)g;

        graphicsSettings.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Draw the background
        graphicsSettings.setPaint(Color.WHITE);
        graphicsSettings.fillRect(0, 0, (int)Dodger.size.getWidth(), (int)Dodger.size.getHeight());

        //Draw the player
        graphicsSettings.setPaint(Color.GREEN);
        graphicsSettings.fillOval((int)Dodger.player.getX(),(int)Dodger.player.getY(),(int)Dodger.player.getSize().getWidth(),(int)Dodger.player.getSize().getHeight());
        graphicsSettings.setPaint(Color.BLACK);
        graphicsSettings.drawOval((int)Dodger.player.getX(),(int)Dodger.player.getY(),(int)Dodger.player.getSize().getWidth(),(int)Dodger.player.getSize().getHeight());

        //Draw the enemies
        for(Enemy e : new ArrayList<>(Dodger.enemies)) {  //Copy the array here to prevent a ConcurrentModificationException
            graphicsSettings.setPaint(Color.red);
            graphicsSettings.fillRect((int)e.getX(), (int)e.getY(), (int)e.getWidth(), (int)e.getHeight());
            graphicsSettings.setPaint(Color.BLACK);
            graphicsSettings.drawRect((int)e.getX(), (int)e.getY(), (int)e.getWidth(), (int)e.getHeight());
        }
    }

}

class KeyBinder extends AbstractAction {
    private String cmd;

    public KeyBinder(String cmd){
        this.cmd = cmd;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //If a key was released, remove it from the keys held
        if(cmd.equals("restart")) {
            Dodger.startGame();
        }
        else if(cmd.equals("pause")) {
            if(!Dodger.gamePaused) {
                Dodger.pause();
            } else {
                Dodger.unpause();
            }
        }
        else if(cmd.contains("released")) {
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