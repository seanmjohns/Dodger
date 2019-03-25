package com.pigcoder.dodger;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;

public class Dodger {

    //The window
    public static JFrame screen;

    //The main area
    public static JPanel area;

    //The player instance
    public static Player player;

    //Stores the score (Which is just seconds passed)
    public static int score;

    //Stores the selected menu button
    public static int selectedButton = 1;

    //All enemies
    public static ArrayList<Enemy> enemies = new ArrayList<>();

    //All powerups in the gameArea
    public static ArrayList<Powerup> powerups = new ArrayList<>();

    //All keys
    public static ArrayList<String> keysHeld = new ArrayList<>();

    //Screen dimensions
    public static Dimension size = new Dimension(400,400);

    //Whether or not the player has died
    public static boolean gameOver = false;

    //Whether or not the game is paused
    public static boolean gamePaused = false;

    //Whether a game is in progress
    public static boolean gameInProgress = false;

    //Repaint every 16 milliseconds (About 60 fps)
    public static Timer repainter = new Timer(16, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            area.repaint();
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

        repainter.start();

        screen = new JFrame();
        screen.setResizable(false);

        //Set basic things
        screen.setTitle("Pigcoder's Dodger");
        screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //When the window is not focused the game needs to be paused
        screen.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) { }

            @Override
            public void windowLostFocus(WindowEvent e) {
                if(!gamePaused) { keysHeld.clear(); }
                if(gameInProgress) { pause(); }
            }
        });

        //Set keybinds
        InputMap im = screen.getRootPane().getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = screen.getRootPane().getActionMap();

        //Movement
        im.put(KeyStroke.getKeyStroke("W"), "up");
        im.put(KeyStroke.getKeyStroke("A"), "left");
        im.put(KeyStroke.getKeyStroke("S"), "down");
        im.put(KeyStroke.getKeyStroke("D"), "right");
        im.put(KeyStroke.getKeyStroke("UP"), "up");
        im.put(KeyStroke.getKeyStroke("LEFT"), "left");
        im.put(KeyStroke.getKeyStroke("DOWN"), "down");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "right");
        im.put(KeyStroke.getKeyStroke("SPACE"), "powerup");
        im.put(KeyStroke.getKeyStroke("ENTER"), "select");
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "quit");

        im.put(KeyStroke.getKeyStroke("R"), "restart");
        im.put(KeyStroke.getKeyStroke("E"), "pause");

        am.put("up", new KeyBinder("up"));
        am.put("left", new KeyBinder("left"));
        am.put("down", new KeyBinder("down"));
        am.put("right", new KeyBinder("right"));
        am.put("restart", new KeyBinder("restart"));
        am.put("pause", new KeyBinder("pause"));
        am.put("powerup", new KeyBinder("powerup"));
        am.put("select", new KeyBinder("select"));
        am.put("quit", new KeyBinder("quit"));

        //Released keybinder
        im.put(KeyStroke.getKeyStroke("released W"), "upreleased");
        im.put(KeyStroke.getKeyStroke("released A"), "leftreleased");
        im.put(KeyStroke.getKeyStroke("released S"), "downreleased");
        im.put(KeyStroke.getKeyStroke("released D"), "rightreleased");
        im.put(KeyStroke.getKeyStroke("released UP"), "upreleased");
        im.put(KeyStroke.getKeyStroke("released LEFT"), "leftreleased");
        im.put(KeyStroke.getKeyStroke("released DOWN"), "downreleased");
        im.put(KeyStroke.getKeyStroke("released RIGHT"), "rightreleased");

        am.put("upreleased", new KeyBinder("releasedup"));
        am.put("leftreleased", new KeyBinder("releasedleft"));
        am.put("downreleased", new KeyBinder("releaseddown"));
        am.put("rightreleased", new KeyBinder("releasedright"));

        screen.setVisible(true);

        goToMenu();

    }

    public static void startGame() {
        //Set the player's position
        area = new GameArea();
        area.setPreferredSize(size);
        screen.setContentPane(area);
        screen.pack();

        gameOver = false;
        score = 0;
        enemies.clear();
        powerups.clear();
        player = new Player((int)size.getWidth()/2, (int)size.getHeight()/2);
        unpause(); //If the game was paused, then unpause it
        gameInProgress = true;
    }


    public static void goToMenu() {
        area = new MenuArea();
        area.setPreferredSize(size);
        screen.setContentPane(area);
        screen.pack();

        enemyCreator.stop();
        enemyMover.stop();
        inputManager.stop();
        scoreKeeper.stop();
        powerupAdder.stop();

        enemies.clear();
        player = null;
        gameInProgress = false;
        gamePaused = false;
        gameOver = false;
        keysHeld.clear();

        enemyCreator.start();
        enemyMover.start();
    }

    public static void pause() {
        enemyCreator.stop();
        enemyMover.stop();
        inputManager.stop();
        scoreKeeper.stop();
        powerupAdder.stop();
        gamePaused = true;
    }

    public static void unpause() {
        enemyCreator.start();
        enemyMover.start();
        inputManager.start();
        scoreKeeper.start();
        powerupAdder.start();
        gamePaused = false;
    }

    public static void died() {
        enemyCreator.stop();
        enemyMover.stop();
        inputManager.stop();
        scoreKeeper.stop();
        powerupAdder.stop();
    }

    private static class MenuArea extends JPanel {

        public static int numberOfButtons = 2;

        public static int width = MenuButton.buttonWidth + 15*2;
        public static int height = 16*numberOfButtons + MenuButton.distanceBetweenButtons*(numberOfButtons) + 15*2;

        public ArrayList<MenuButton> buttons = new ArrayList<>();

        public MenuArea() {
            buttons.add(new MenuButton(
                    (int)(size.getWidth()/2 - MenuButton.buttonWidth/2),
                    (int)(size.getHeight()/2 - (MenuButton.buttonHeight*numberOfButtons/2 + MenuButton.distanceBetweenButtons*(numberOfButtons-1)/2)),
                    "PLAY", 1));
            buttons.add(new MenuButton(
                    (int)(size.getWidth()/2 - MenuButton.buttonWidth/2),
                    (int)(size.getHeight()/2 + (MenuButton.buttonHeight*numberOfButtons/2 - MenuButton.distanceBetweenButtons*(numberOfButtons-1)/2)),
                    "QUIT", 2));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D graphicsSettings = (Graphics2D)g;
            FontMetrics fm = graphicsSettings.getFontMetrics();

            graphicsSettings.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //Draw the background
            graphicsSettings.setPaint(Color.WHITE);
            graphicsSettings.fillRect(0, 0, (int)Dodger.size.getWidth(), (int)Dodger.size.getHeight());

            //Draw the enemies
            for(Enemy e : new ArrayList<>(Dodger.enemies)) {  //Copy the array here to prevent a ConcurrentModificationException
                graphicsSettings.setPaint(Color.red);
                graphicsSettings.fillRect((int)e.getX(), (int)e.getY(), (int)e.getWidth(), (int)e.getHeight());
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawRect((int)e.getX(), (int)e.getY(), (int)e.getWidth(), (int)e.getHeight());
            }

            //Draw the menu background
            int x = (int)(size.getWidth()/2 - width/2);
            int y = (int)(size.getHeight()/2 - height/2);
            graphicsSettings.setPaint(new Color(0,0,0, 64)); //Translucent
            graphicsSettings.fillRoundRect(x, y, width, height, 20, 20);
            graphicsSettings.setPaint(new Color(0,0,0, 127)); //Translucent
            graphicsSettings.drawRoundRect(x, y, width, height, 20, 20);

            //Draw the buttons
	        for(MenuButton mb : buttons) {
				mb.draw(g);
	        }
        }

        private static class MenuButton {

            public static int distanceBetweenButtons = 20;

            public static int buttonWidth = 100;
            public static int buttonHeight = 25;

            public Color color = new Color(255,0,0, 191);

            public int buttonNumber;

            public int x;
            public int y;

            public String text;

            public void draw(Graphics g) {
                if(selectedButton == buttonNumber) {
                    g.setColor(new Color(255,0,0, 255));
                } else {
                    g.setColor(color);
                }
                g.fillRoundRect(x, y, buttonWidth, buttonHeight, 10,10);
                g.setColor(new Color(0,0,0, 127));
                g.drawRoundRect(x, y, buttonWidth, buttonHeight, 10,10);
                g.setColor(Color.BLACK);
                g.drawString(text, (int)size.getWidth()/2 - g.getFontMetrics().stringWidth(text)/2,y + buttonHeight/2 + g.getFontMetrics().getHeight()/2);
                color = new Color(255,0,0, 191);
            }

	        public MenuButton(int x, int y, String text, int buttonNumber) {
		        this.x = x;
		        this.y = y;
		        this.text = text;
                this.buttonNumber = buttonNumber;
	        }


        }

    }

    private static class GameArea extends JPanel {

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D graphicsSettings = (Graphics2D)g;
            FontMetrics fm = graphicsSettings.getFontMetrics();

            graphicsSettings.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //Draw the background
            graphicsSettings.setPaint(Color.WHITE);
            graphicsSettings.fillRect(0, 0, (int)Dodger.size.getWidth(), (int)Dodger.size.getHeight());

            //Draw powerups
            for(Powerup p : Dodger.powerups) {
                if(p.type == 1) { // Brake
                    graphicsSettings.setPaint(Color.BLUE);
                } else if (p.type == 2) { // Speed Boost
                    graphicsSettings.setPaint(Color.CYAN);
                }
                graphicsSettings.fillRect((int)p.x, (int)p.y, (int)Powerup.SIZE.getWidth(), (int)Powerup.SIZE.getHeight());
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawRect((int)p.x, (int)p.y, (int)Powerup.SIZE.getWidth(), (int)Powerup.SIZE.getHeight());
            }

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

            //Display the score
            graphicsSettings.setPaint(Color.BLACK);
            graphicsSettings.drawString(Integer.toString(Dodger.score), 10, 20);

            //If the game is paused, say so
            if(Dodger.gamePaused) {
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawString("PAUSED", (int)Dodger.size.getWidth() - fm.stringWidth("PAUSED") - 10, 20);
            }

            //Display the controls. Note the 16; that is the height of the font
            //Background
            graphicsSettings.setPaint(new Color(0,0,0, 64)); //Translucent
            graphicsSettings.fillRoundRect(5, (int)Dodger.size.getHeight() - fm.getHeight() - 5*5 - 16*5, fm.stringWidth("Move - WASD / Arrows") + 10, fm.getHeight() + 5*4 + 16*5, 10, 10);
            graphicsSettings.setPaint(new Color(0,0,0, 127)); //Translucent
            graphicsSettings.drawRoundRect(5, (int)Dodger.size.getHeight() - fm.getHeight() - 5*5 - 16*5, fm.stringWidth("Move - WASD / Arrows") + 10, fm.getHeight() + 5*4 + 16*5, 10, 10);
            //The text
            graphicsSettings.setPaint(Color.BLACK);
            graphicsSettings.drawString("Quit - ESCAPE", 10, (int)Dodger.size.getHeight() - fm.getHeight());
            graphicsSettings.drawString("Powerup - SPACE", 10, (int)Dodger.size.getHeight() - fm.getHeight() - 5 - 16);
            graphicsSettings.drawString("Restart - R", 10, (int)Dodger.size.getHeight() - fm.getHeight() - 5*2 - 16*2);
            graphicsSettings.drawString("Pause - E", 10, (int)Dodger.size.getHeight() - fm.getHeight() - 5*3 - 16*3);
            graphicsSettings.drawString("Move - WASD / Arrows", 10, (int)Dodger.size.getHeight() - fm.getHeight() - 5*4 - 16*4);

            //Draw the current powerup
            if(Dodger.player.getStoredPowerup() != -1) {
                if(Dodger.player.getStoredPowerup() == 1) {
                    graphicsSettings.setPaint(Color.BLUE);
                } else if (Dodger.player.getStoredPowerup() == 2) {
                    graphicsSettings.setPaint(Color.CYAN);
                }
                graphicsSettings.fillRect((int)Dodger.size.getWidth() - 15, (int)Dodger.size.getHeight() - 15, 10, 10);
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawRect((int)Dodger.size.getWidth() - 15, (int)Dodger.size.getHeight() - 15, 10, 10);
            }
        }

    }

    private static class KeyBinder extends AbstractAction {
        private String cmd;

        public KeyBinder(String cmd) {
            this.cmd = cmd;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(gameInProgress) { //In-Game input
                if(!(area instanceof GameArea)) { return; }
                if (cmd.equals("restart")) {
                    Dodger.startGame();
                } else if (cmd.equals("pause") && !Dodger.gameOver) {
                    if (!Dodger.gamePaused) {
                        Dodger.pause();
                    } else {
                        Dodger.unpause();
                    }
                } else if (cmd.equals("powerup") && Dodger.player.getStoredPowerup() != -1) {
                    int type = Dodger.player.getStoredPowerup();
                    if (type == 1) { //Brake
                        Dodger.player.setxVel(0);
                        Dodger.player.setyVel(0);
                    } else if (type == 2) { //SpeedBoost. The values are fine tuned so there is no way to tell what would be a good value here without playing
                        if (Dodger.keysHeld.contains("up")) {
                            Dodger.player.decreaseyVel(3.5);
                        }
                        if (Dodger.keysHeld.contains("down")) {
                            Dodger.player.increaseyVel(3.5);
                        }
                        if (Dodger.keysHeld.contains("left")) {
                            Dodger.player.decreasexVel(3.5);
                        }
                        if (Dodger.keysHeld.contains("right")) {
                            Dodger.player.increasexVel(3.5);
                        }

                    }
                    Dodger.player.setStoredPowerup(-1);
                } else if (cmd.contains("released")) {
                    Dodger.keysHeld.remove(cmd.substring(8));
                } else if (cmd.contains("quit")) {
                    goToMenu();
                }
                //If a new key was pressed, add it
                else {
                    if (!Dodger.keysHeld.contains(cmd)) {
                        Dodger.keysHeld.add(cmd);
                    }
                }
            } else { //Menu input
                if(!(area instanceof MenuArea)) { return; }
                if(cmd.equals("up")) {
                    selectedButton--;
                    if(selectedButton < 1) {
                        selectedButton = 1;
                    }
                } if(cmd.equals("down")) {
                    selectedButton++;
                    if(selectedButton > 2) {
                        selectedButton = 2;
                    }
                } if(cmd.equals("select")) {
                    if(selectedButton == 1) {
                        startGame();
                    } else if(selectedButton == 2) {
                        System.exit(0);
                    }
                }
            }
        }

    }

    //Create enemies every 100 milliseconds
    public static Timer enemyCreator = new Timer(100, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!gameOver) {
                //Create a new enemy

                //A random size between the minimum size and maximum size, the enemy is always a square
                int size = ThreadLocalRandom.current().nextInt(Enemy.sizeBoundaryLower, Enemy.sizeBoundaryUpper);

                //A random speed (1 or 2)
                double speed = Math.random()*1.5 + 0.5;

                enemies.add(new Enemy(ThreadLocalRandom.current().nextInt(0, (int)Dodger.size.getWidth() - size), (int)Dodger.size.getHeight(), new Dimension(size, size), speed));

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
            if(!gameOver) {
                for(Enemy enemy : enemies) {
                    enemy.move();
                    if (player != null && player.intersects(enemy)) {
                        died();
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
            //In-game input
            if(!gameOver && !gamePaused && gameInProgress) {
                if(player == null) { return; }
                //Vertical movement
                if(keysHeld.contains("up")) {
                    player.decreaseyVel(0.15);
                }
                else if(keysHeld.contains("down")) {
                    player.increaseyVel(0.15);
                }
                else {//Slow the player down if no keys are being pressed
                    player.decreaseyVel(player.getyVel() / 15); //Cuts speed to 15%
                }

                //Horizontal movement
                if(keysHeld.contains("left")) {
                    player.decreasexVel(0.15);
                }
                else if(keysHeld.contains("right")) {
                    player.increasexVel(0.15);
                }
                else { //Slow the player down if no keys are being pressed
                    player.decreasexVel(player.getxVel() / 15); //Cuts speed to 15%
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

                //Check to see if they have run into a powerup
                ArrayList<Powerup> newPowerups = new ArrayList<>();
                for(Powerup p : powerups) {
                    if(player.getStoredPowerup() != -1) { newPowerups.add(p); continue; }
                    if(player.intersects(p.x, p.y, (int)Powerup.SIZE.getWidth(), (int)Powerup.SIZE.getHeight())){
                        player.setStoredPowerup(p.type);
                        continue;
                    }
                    newPowerups.add(p);
                }
                Dodger.powerups = newPowerups;
            } else { //Menu area

            }
        }
    });

    //Increase the score every second
    public static Timer scoreKeeper = new Timer(1000, new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(player == null) { return; }
            score++;
        }
    });

    //Add a powerup to the screen every 5 seconds
    public static Timer powerupAdder = new Timer(5000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(player == null) { return; }
            //Type can only be 1 or 2
            int type = ThreadLocalRandom.current().nextInt(1, Powerup.numberOfpowerUpTypes + 1);
            //Position
            int x = ThreadLocalRandom.current().nextInt(0, (int)(Dodger.size.getWidth() - Powerup.SIZE.getWidth() + 1));
            int y = ThreadLocalRandom.current().nextInt(0, (int)(Dodger.size.getHeight() - Powerup.SIZE.getHeight() + 1));
            //Create it
            powerups.add(new Powerup(type, x, y));
        }
    });
}