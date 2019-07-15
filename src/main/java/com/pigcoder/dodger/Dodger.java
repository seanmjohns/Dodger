package com.pigcoder.dodger;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import java.awt.geom.*;
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

    //Whether or not the timers have been made
    public static boolean timersCreated = false;

    //Whether or not the background should be disabled in the menu
    public static boolean backgroundDisabled = false;

    //The difficulty 1: easy, 2: normal, 3: hard
    public static int difficulty = 2;

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
            if(area != null ) {area.repaint(); }
        }
    });

    public static Timer enemyCreator;

    public static Timer enemyMover;

    public static Timer inputManager;

    public static Timer scoreKeeper;

    public static Timer powerupAdder;

    public static Timer collisionManager;

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
                if(gameInProgress && !((GameArea)area).waitingToStart) { pause(); }
            }
        });

        screen.setVisible(true);

        area = new MenuArea();
        area.setPreferredSize(size);
        screen.setContentPane(area);
        screen.pack();

        //Set keybinds
        createKeybinds();

        createTimers();

        goToMenu();

    }

    public static void startGame(int diff) {
        //Set the player's position
        area = new GameArea();
        area.setPreferredSize(size);
        screen.setContentPane(area);
        screen.pack();

        difficulty = diff;

        gameOver = false;
        score = 0;
        enemies.clear();
        powerups.clear();
        player = new Player((int)(size.getWidth()/2 - Player.defaultSize.getWidth()/2), (int)(size.getHeight()/2 - Player.defaultSize.getHeight()/2));
        unpause(); //If the game was paused, then unpause it
        if(difficulty == 1) { //Set enemy creation rate based on difficulty
            enemyCreator.setDelay(300);
        } else if (difficulty == 2) {
            enemyCreator.setDelay(200);
        } else if (difficulty == 3) {
            enemyCreator.setDelay(100);
        }
        gameInProgress = true;
    }


    public static void goToMenu() {
        area = new MenuArea();
        area.setPreferredSize(size);
        screen.setContentPane(area);
        screen.pack();

        stopAllTimers();

        enemies.clear();
        player = null;
        gameInProgress = false;
        gamePaused = false;
        gameOver = false;
        ((MenuArea)area).inDifficultyMenu = false;
        score = 0;
        keysHeld.clear();
        enemyCreator.setDelay(100); //Reset the enemy creation rate back to 1/10 of a second

        enemyCreator.start();
        enemyMover.start();
    }

    public static void pause() {
        stopAllTimers();
        gamePaused = true;
    }

    public static void unpause() {
        startAllTimers();
        gamePaused = false;
    }

    public static void died() {
        stopAllTimers();
        gameOver = true;
    }

    public static void stopAllTimers() {  //Not including the repainter
        enemyCreator.stop();
        enemyMover.stop();
        inputManager.stop();
        scoreKeeper.stop();
        powerupAdder.stop();
        collisionManager.stop();
    }

    public static void startAllTimers() {  //Not including the repainter
        enemyCreator.start();
        enemyMover.start();
        inputManager.start();
        scoreKeeper.start();
        powerupAdder.start();
        collisionManager.start();
    }


    private static class MenuArea extends JPanel {

        public int xOffset = 0;
        public int yOffset = 0;


        //Main menu stuff
        public static String[] mainMenuButtonTexts = {"PLAY", "HELP", "QUIT"};

        public static int numberOfMainMenuButtons = mainMenuButtonTexts.length;

        public static int mainMenuWidth = MenuButton.buttonWidth + 15*2;
        public static int mainMenuHeight = 16*numberOfMainMenuButtons + MenuButton.distanceBetweenButtons*(numberOfMainMenuButtons) + 15*2;

        //Difficulty menu stuff
        public static String[] difficultyMenuButtonTexts = {"EASY", "NORMAL", "HARD", "BACK"};

        public static int numberOfDifficultyMenuButtons = difficultyMenuButtonTexts.length;

        public static int difficultyMenuWidth = MenuButton.buttonWidth + 15*2;
        public static int difficultyMenuHeight = 16*numberOfDifficultyMenuButtons + MenuButton.distanceBetweenButtons*(numberOfDifficultyMenuButtons) + 15*2;

        public static int helpWidth = (int)size.getWidth() * 9/10;
        public static int helpHeight = (int)size.getWidth() * 9/10;

        public ArrayList<MenuButton> mainMenuButtons = new ArrayList<>();
        public ArrayList<MenuButton> difficultyMenuButtons = new ArrayList<>();

        public boolean inHelpMenu = false;

        public boolean inDifficultyMenu = false;

        public MenuArea() {
            //main menu
            int mainMenuX = (int)(size.getWidth() / 2 - MenuButton.buttonWidth / 2);
            int mainMenuY = (int)(size.getHeight() / 2 - (MenuButton.buttonHeight * numberOfMainMenuButtons / 2 + MenuButton.distanceBetweenButtons * (numberOfMainMenuButtons - 1) / 2));
            for(int i=0; i < numberOfMainMenuButtons; i++) {
                mainMenuButtons.add(new MenuButton(mainMenuX, mainMenuY, mainMenuButtonTexts[i], i + 1));
                mainMenuY = mainMenuY + (MenuButton.distanceBetweenButtons + MenuButton.buttonHeight);
            }
            //difficulty menu
            int difficultyMenuX = (int)(size.getWidth() / 2 - MenuButton.buttonWidth / 2);
            int difficultyMenuY = (int)(size.getHeight() / 2 - (MenuButton.buttonHeight * numberOfDifficultyMenuButtons / 2 + MenuButton.distanceBetweenButtons * (numberOfDifficultyMenuButtons - 1) / 2));
            for(int i=0; i < numberOfDifficultyMenuButtons; i++) {
                difficultyMenuButtons.add(new MenuButton(difficultyMenuX, difficultyMenuY, difficultyMenuButtonTexts[i], i + 1));
                difficultyMenuY = difficultyMenuY + (MenuButton.distanceBetweenButtons + MenuButton.buttonHeight);
            }

            //Create mouselisteners
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (selectedButton != -1) {
                        if(inDifficultyMenu) {
                            if (selectedButton == 1) {
                                startGame(1);
                            } else if (selectedButton == 2) {
                                startGame(2);
                            } else if (selectedButton == 3) {
                                startGame(3);
                            } else if (selectedButton == 4) {
                                inDifficultyMenu = false;
                            }
                        } else {
                            if (selectedButton == 1) {
                                inDifficultyMenu = true;
                            } else if (selectedButton == 2) {
                                inHelpMenu = true;
                            } else if (selectedButton == 3) {
                                System.exit(0);
                            }
                        }
                    }
                }
            });
            this.addMouseMotionListener(new MouseMotionAdapter() {

                @Override
                public void mouseMoved(MouseEvent e) {
                    if(inDifficultyMenu) {
                        for (MenuButton mb : difficultyMenuButtons) {
                            if (mb.contains(e.getPoint())) {
                                selectedButton = mb.buttonNumber;
                                return;
                            }
                        }
                    } else {
                        for (MenuButton mb : mainMenuButtons) {
                            if (mb.contains(e.getPoint())) {
                                selectedButton = mb.buttonNumber;
                                return;
                            }
                        }
                    }
                    //If getting here, then it is not contained in any button
                    selectedButton = 0;
                }
            });
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

            if(inHelpMenu) {
                //Draw the help background
                int x = (int) ((size.getWidth() - helpWidth) / 2);
                int y = (int) ((size.getHeight() - helpHeight) / 2);
                graphicsSettings.setPaint(new Color(0, 0, 0, 127)); //Translucent
                graphicsSettings.fillRoundRect(x, y, helpWidth, helpHeight, 20, 20);
                graphicsSettings.setPaint(new Color(0, 0, 0, 159)); //Translucent
                graphicsSettings.drawRoundRect(x, y, helpWidth, helpHeight, 20, 20);

                //Draw the heading
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.setFont(new Font(fm.getFont().getFontName(), Font.BOLD, 25));
                fm = graphicsSettings.getFontMetrics();
                int helpFontHeight = fm.getHeight();
                graphicsSettings.drawString("HELP", (int) (size.getWidth() / 2 - fm.stringWidth("HELP") / 2), y + 40);
                graphicsSettings.drawLine((int) (size.getWidth() / 5), y + fm.getHeight() + 20, (int) (size.getWidth() * 4 / 5), y + helpFontHeight + 20);

                //Draw the main objective
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.setFont(new Font(fm.getFont().getFontName(), Font.BOLD, 10));
                //Draw the picture of the player
                fm = graphicsSettings.getFontMetrics();
                int mainObjectiveFontHeight = fm.getHeight();
                graphicsSettings.drawString("THIS IS YOU", (int) (size.getWidth() / 3 - fm.stringWidth("THIS IS YOU") / 2), y + helpFontHeight + 20 + 30);
                graphicsSettings.setPaint(Color.GREEN);
                graphicsSettings.fillOval((int) (size.getWidth() / 3 - 20 / 2), y + helpFontHeight + 20 + 30 + mainObjectiveFontHeight, 20, 20);
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawOval((int) (size.getWidth() / 3 - 20 / 2), y + helpFontHeight + 20 + 30 + mainObjectiveFontHeight, 20, 20);
                //Draw the picture of the enemy
                fm = graphicsSettings.getFontMetrics();
                graphicsSettings.drawString("AVOID THIS", (int) (size.getWidth() / 3 * 2 - fm.stringWidth("AVOID THIS") / 2), y + helpFontHeight + 20 + 30);
                graphicsSettings.setPaint(Color.LIGHT_GRAY);
                graphicsSettings.fillRect((int) (size.getWidth() / 3 * 2 - 20 / 2), y + helpFontHeight + 20 + 30 + mainObjectiveFontHeight, 20, 20);
                graphicsSettings.setPaint(Color.RED);
                graphicsSettings.fillRect((int) (size.getWidth() / 3 * 2 - 20 / 2), y + helpFontHeight + 20 + 30 + mainObjectiveFontHeight, 20, 20);
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawRect((int) (size.getWidth() / 3 * 2 - 20 / 2), y + helpFontHeight + 20 + 30 + mainObjectiveFontHeight, 20, 20);

                //Draw the powerup help
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.setFont(new Font(fm.getFont().getFontName(), Font.BOLD, 15));
                fm = graphicsSettings.getFontMetrics();
                int powerupHeadingY = y + helpFontHeight + 20 + 30 + mainObjectiveFontHeight + 30 + 17;
                int powerupFontHeight = fm.getHeight();
                graphicsSettings.drawString("POWERUPS - ONE AT A TIME", (int) (size.getWidth() / 2 - fm.stringWidth("POWERUPS - ONE AT A TIME") / 2), powerupHeadingY);
                //Draw the brake powerup
                graphicsSettings.setFont(new Font(fm.getFont().getFontName(), Font.BOLD, 10));
                fm = graphicsSettings.getFontMetrics();
                int powerupNameHeight = fm.getHeight();
                graphicsSettings.drawString("BRAKE", (int) (size.getWidth() / 4 - fm.stringWidth("BRAKE") / 2), powerupHeadingY + powerupFontHeight);
                graphicsSettings.setPaint(Color.BLUE);
                graphicsSettings.fillRect((int) (size.getWidth() / 4 - 15 / 2), powerupHeadingY + powerupFontHeight + powerupNameHeight, 15, 15);
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawRect((int) (size.getWidth() / 4 - 15 / 2), powerupHeadingY + powerupFontHeight + powerupNameHeight, 15, 15);
                //Draw the speed boost powerup
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawString("SPEED BOOST", (int) (size.getWidth() / 4 * 2 - fm.stringWidth("SPEED BOOST") / 2), powerupHeadingY + powerupFontHeight);
                graphicsSettings.setPaint(Color.CYAN);
                graphicsSettings.fillRect((int) (size.getWidth() / 4 * 2 - 15 / 2), powerupHeadingY + powerupFontHeight + powerupNameHeight, 15, 15);
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawRect((int) (size.getWidth() / 4 * 2 - 15 / 2), powerupHeadingY + powerupFontHeight + powerupNameHeight, 15, 15);
                //Draw the armor powerup
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawString("ARMOR", (int) (size.getWidth() / 4 * 3 - fm.stringWidth("ARMOR") / 2), powerupHeadingY + powerupFontHeight);
                graphicsSettings.setPaint(Color.ORANGE);
                graphicsSettings.fillRect((int) (size.getWidth() / 4 * 3 - 15 / 2), powerupHeadingY + powerupFontHeight + powerupNameHeight, 15, 15);
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawRect((int) (size.getWidth() / 4 * 3 - 15 / 2), powerupHeadingY + powerupFontHeight + powerupNameHeight, 15, 15);
                //Draw the armor description
                int armorDescriptionY = powerupHeadingY + powerupFontHeight + powerupNameHeight + 40;
                graphicsSettings.drawString("ARMOR USED ON BLOCK TOUCH", (int) (size.getWidth()/2 - fm.stringWidth("ARMOR USED ON BLOCK TOUCH") / 2), armorDescriptionY);


                //Draw the controls
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.setFont(new Font(fm.getFont().getFontName(), Font.BOLD, 15));
                fm = graphicsSettings.getFontMetrics();
                int controlHeadingY = 35 + armorDescriptionY;
                int controlHeadingHeight = fm.getHeight();
                graphicsSettings.drawString("CONTROLS", (int) (size.getWidth() / 2 - fm.stringWidth("CONTROLS") / 2), controlHeadingY);
                graphicsSettings.setFont(new Font(fm.getFont().getFontName(), Font.PLAIN, 10));
                fm = graphicsSettings.getFontMetrics();
                int controlRowHeight = fm.getHeight() + 15;
                //Draw the WASD/Arrows movement
                graphicsSettings.drawString("Move: Arrows", (int) size.getWidth() / 3 - fm.stringWidth("Move: Arrows") / 2, controlHeadingY + controlHeadingHeight + 10);
                //Quitting
                graphicsSettings.drawString("Quit: Escape", (int) size.getWidth() / 3 * 2 - fm.stringWidth("Quit: Escape") / 2, controlHeadingY + controlHeadingHeight + 10);
                //Pausing
                graphicsSettings.drawString("Pause: E", (int) size.getWidth() / 3 - fm.stringWidth("Pause: E") / 2, controlHeadingY + controlHeadingHeight + controlRowHeight + 10);
                //Restarting
                graphicsSettings.drawString("Restart: R", (int) size.getWidth() / 3 * 2 - fm.stringWidth("Restart: R") / 2, controlHeadingY + controlHeadingHeight + controlRowHeight + 10);
                //Powerup
                graphicsSettings.drawString("Powerup: Space", (int) size.getWidth() / 3 - fm.stringWidth("Powerup: SPACE") / 2, controlHeadingY + controlHeadingHeight + controlRowHeight * 2 + 10);
                //Menu back
                graphicsSettings.drawString("Menu Background: F", (int) size.getWidth() / 3 * 2 - fm.stringWidth("Menu Background: F") / 2, controlHeadingY + controlHeadingHeight + controlRowHeight * 2 + 10);

                //Press escape to go back
                graphicsSettings.drawString("Press escape to go back", (int) (size.getWidth() - fm.stringWidth("Press escape to go back") - 10), 13);
            } else if (inDifficultyMenu) {
                //Draw the menu background
                int x = (int) (size.getWidth() / 2 - difficultyMenuWidth / 2);
                int y = (int) (size.getHeight() / 2 - difficultyMenuHeight / 2);
                graphicsSettings.setPaint(new Color(0, 0, 0, 127)); //Translucent
                graphicsSettings.fillRoundRect(x, y, difficultyMenuWidth, difficultyMenuHeight, 20, 20);
                graphicsSettings.setPaint(new Color(0, 0, 0, 159)); //Translucent
                graphicsSettings.drawRoundRect(x, y, difficultyMenuWidth, difficultyMenuHeight, 20, 20);

                //Draw the buttons
                for (MenuButton mb : difficultyMenuButtons) {
                    mb.draw(g);
                }
            } else {
                //Draw the menu background
                int x = (int) (size.getWidth() / 2 - mainMenuWidth / 2);
                int y = (int) (size.getHeight() / 2 - mainMenuHeight / 2);
                graphicsSettings.setPaint(new Color(0, 0, 0, 127)); //Translucent
                graphicsSettings.fillRoundRect(x, y, mainMenuWidth, mainMenuHeight, 20, 20);
                graphicsSettings.setPaint(new Color(0, 0, 0, 159)); //Translucent
                graphicsSettings.drawRoundRect(x, y, mainMenuWidth, mainMenuHeight, 20, 20);

                //Draw the buttons
                for (MenuButton mb : mainMenuButtons) {
                    mb.draw(g);
                }
            }
        }

        private static class MenuButton extends Rectangle2D.Double {

            public static int distanceBetweenButtons = 20;

            public static int buttonWidth = 100;
            public static int buttonHeight = 25;

            public Color color = new Color(255,0,0, 150);

            public int buttonNumber;

            public String text;

            public void draw(Graphics g) {
                if(selectedButton == buttonNumber) {
                    g.setColor(new Color(255,0,0, 255));
                } else {
                    g.setColor(color);
                }
                g.fillRoundRect((int)x, (int)y, buttonWidth, buttonHeight, 10,10);
                g.setColor(new Color(0,0,0, 127));
                g.drawRoundRect((int)x, (int)y, buttonWidth, buttonHeight, 10,10);
                g.setColor(Color.BLACK);
                g.drawString(text, (int)size.getWidth()/2 - g.getFontMetrics().stringWidth(text)/2,(int)(y + buttonHeight/2 + (g.getFontMetrics().getHeight()/2)));
                color = new Color(255,0,0, 191);
            }

	        public MenuButton(int x, int y, String text, int buttonNumber) {
		        this.x = x;
		        this.y = y;
		        this.width = buttonWidth;
                this.height = buttonHeight;
		        this.text = text;
                this.buttonNumber = buttonNumber;
	        }
        }

    }

    private static class GameArea extends JPanel {

        public boolean waitingToStart = true;

        public GameArea() { }

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
                } else if (p.type == 3) { // Armor
                    graphicsSettings.setPaint(Color.ORANGE);
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

            if(gamePaused) {
                //Display the controls. Note the 16; that is the height of the font
                //Background
                graphicsSettings.setPaint(new Color(0, 0, 0, 64)); //Translucent
                graphicsSettings.fillRoundRect(5, (int) Dodger.size.getHeight() - fm.getHeight() - 5 * 5 - 16 * 5, fm.stringWidth("Pause/Controls - E") + 10, fm.getHeight() + 5 * 4 + 16 * 5, 10, 10);
                graphicsSettings.setPaint(new Color(0, 0, 0, 127)); //Translucent
                graphicsSettings.drawRoundRect(5, (int) Dodger.size.getHeight() - fm.getHeight() - 5 * 5 - 16 * 5, fm.stringWidth("Pause/Controls - E") + 10, fm.getHeight() + 5 * 4 + 16 * 5, 10, 10);
                //The text
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawString("Quit - Escape", 10, (int) Dodger.size.getHeight() - fm.getHeight());
                graphicsSettings.drawString("Pause - E", 10, (int) Dodger.size.getHeight() - fm.getHeight() - 5 - 16);
                graphicsSettings.drawString("Powerup - Space", 10, (int) Dodger.size.getHeight() - fm.getHeight() - 5 * 2 - 16 * 2);
                graphicsSettings.drawString("Restart - R", 10, (int) Dodger.size.getHeight() - fm.getHeight() - 5 * 3 - 16 * 3);
                graphicsSettings.drawString("Move - Arrows", 10, (int) Dodger.size.getHeight() - fm.getHeight() - 5 * 4 - 16 * 4);
            } else {
                //Display the pause and quit controls.
                graphicsSettings.setPaint(new Color(0, 0, 0, 64)); //Translucent
                graphicsSettings.fillRoundRect(5, (int) Dodger.size.getHeight() - fm.getHeight() - 5 * 2 - 16 * 2, fm.stringWidth("Pause/Controls - E") + 10, fm.getHeight() + 5 + 16 * 2, 10, 10);
                graphicsSettings.setPaint(new Color(0, 0, 0, 127)); //Translucent
                graphicsSettings.drawRoundRect(5, (int) Dodger.size.getHeight() - fm.getHeight() - 5 * 2 - 16 * 2, fm.stringWidth("Pause/Controls - E") + 10, fm.getHeight() + 5 + 16 * 2, 10, 10);
                //The text
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawString("Quit - Escape", 10, (int) Dodger.size.getHeight() - fm.getHeight());
                graphicsSettings.drawString("Pause/Controls - E", 10, (int) Dodger.size.getHeight() - fm.getHeight() - 5 - 16 );
            }

            //Draw the current powerup
            if(Dodger.player.getStoredPowerup() != -1) {
                if(Dodger.player.getStoredPowerup() == 1) { //Brake
                    graphicsSettings.setPaint(Color.BLUE);
                } else if (Dodger.player.getStoredPowerup() == 2) { //Speed boost
                    graphicsSettings.setPaint(Color.CYAN);
                } else if (Dodger.player.getStoredPowerup() == 3) { //Armor
                    graphicsSettings.setPaint(Color.ORANGE);
                }
                graphicsSettings.fillRect((int)Dodger.size.getWidth() - 15, (int)Dodger.size.getHeight() - 15, 10, 10);
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawRect((int)Dodger.size.getWidth() - 15, (int)Dodger.size.getHeight() - 15, 10, 10);
            }

            if(gameOver) {
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawString("Press R to restart", (int)size.getWidth()/2 - graphicsSettings.getFontMetrics().stringWidth("Press R to restart")/2, 20);
            }

            if(waitingToStart) {
                graphicsSettings.setFont(new Font(fm.getFont().getFontName(), Font.BOLD, 12));
                graphicsSettings.setPaint(Color.BLACK);
                graphicsSettings.drawString("Press SPACE to Start", (int)size.getWidth()/2 - graphicsSettings.getFontMetrics().stringWidth("Press SPACE to Start")/2, (int)size.getHeight()/4 - graphicsSettings.getFontMetrics().getHeight()/2);
            }

            //Draw the difficulty
            graphicsSettings.setPaint(Color.BLACK);
            graphicsSettings.setFont(new Font(fm.getFont().getFontName(), Font.BOLD, 12));
            String text = "easy";
            if(difficulty == 1) {
                text = "EASY";
            } else if(difficulty == 2) {
                text = "NORMAL";
            } else if(difficulty == 3) {
                text = "HARD";
            }
            graphicsSettings.drawString(text, (int)size.getWidth()/2 - graphicsSettings.getFontMetrics().stringWidth(text)/2, (int)size.getHeight() - graphicsSettings.getFontMetrics().getHeight() - 10);

        }

    }

    public static void createKeybinds() {

        //Add key listeners
        InputMap im = screen.getRootPane().getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = screen.getRootPane().getRootPane().getActionMap();
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
        im.put(KeyStroke.getKeyStroke("F"),  "disableBackground");

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
        am.put("disableBackground", new KeyBinder("disableBackground"));

        //Released keys
        im.put(KeyStroke.getKeyStroke("released W"), "releasedup");
        im.put(KeyStroke.getKeyStroke("released A"), "releasedleft");
        im.put(KeyStroke.getKeyStroke("released S"), "releaseddown");
        im.put(KeyStroke.getKeyStroke("released D"), "releasedright");
        im.put(KeyStroke.getKeyStroke("released UP"), "releasedup");
        im.put(KeyStroke.getKeyStroke("released LEFT"), "releasedleft");
        im.put(KeyStroke.getKeyStroke("released DOWN"), "releaseddown");
        im.put(KeyStroke.getKeyStroke("released RIGHT"), "releasedright");

        am.put("releasedup", new KeyBinder("releasedup"));
        am.put("releasedleft", new KeyBinder("releasedleft"));
        am.put("releaseddown", new KeyBinder("releaseddown"));
        am.put("releasedright", new KeyBinder("releasedright"));

    }

    private static class KeyBinder extends AbstractAction {
        private String cmd;

        public KeyBinder(String cmd) {
            this.cmd = cmd;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(area instanceof GameArea) { //In-Game input
                if (cmd.equals("restart")) {
                    Dodger.startGame(difficulty);
                } else if (cmd.equals("pause") && !Dodger.gameOver && !((GameArea)area).waitingToStart) {
                    if (!Dodger.gamePaused) {
                        Dodger.pause();
                    } else {
                        Dodger.unpause();
                    }
                } else if (cmd.equals("powerup")) {
                    if(((GameArea)area).waitingToStart) {
                        ((GameArea)area).waitingToStart = false;
                    } else if (player.getStoredPowerup() != -1) {
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
                        } else if (type == 3) { } // This is the armor powerup, so it doesnt do anything
                        if (type != 3) {
                            Dodger.player.setStoredPowerup(-1);
                        }
                    }
                } else if (cmd.contains("released")) {
                    Dodger.keysHeld.remove(cmd.substring(8));
                } else if (cmd.equals("quit")) {
                    goToMenu();
                }
                //If a new key was pressed, add it
                else {
                    if (!Dodger.keysHeld.contains(cmd)) {
                        Dodger.keysHeld.add(cmd);
                    }
                }
            } else if(area instanceof MenuArea) { //Menu input
                if(cmd.equals("disableBackground")) {
                    if(!backgroundDisabled) {
                        enemies.clear();
                        backgroundDisabled = true;
                    } else {
                        backgroundDisabled = false;
                    }
                }
                if(((MenuArea)area).inHelpMenu) {
                    if (cmd.equals("quit") || cmd.equals("select")) {
                        ((MenuArea) area).inHelpMenu = false;
                    }
                } else if (((MenuArea)area).inDifficultyMenu) {
                    if (cmd.equals("up")) {
                        selectedButton--;
                        if (selectedButton < 1) {
                            selectedButton = 1;
                        }
                    } else if (cmd.equals("down")) {
                        selectedButton++;
                        if (selectedButton > MenuArea.numberOfDifficultyMenuButtons) {
                            selectedButton = MenuArea.numberOfDifficultyMenuButtons;
                        }
                    } else if (cmd.equals("select")) {
                        if (selectedButton == 1) {
                            startGame(1);
                        } else if (selectedButton == 2) {
                            startGame(2);
                        } else if (selectedButton == 3) {
                            startGame(3);
                        } else if (selectedButton == 4) {
                            ((MenuArea) area).inDifficultyMenu = false;
                        }
                    } else if (cmd.equals("quit")) {
                        ((MenuArea)area).inDifficultyMenu = false;
                    }
                } else {
                    if (cmd.equals("up")) {
                        selectedButton--;
                        if (selectedButton < 1) {
                            selectedButton = 1;
                        }
                    } else if (cmd.equals("down")) {
                        selectedButton++;
                        if (selectedButton > MenuArea.numberOfMainMenuButtons) {
                            selectedButton = MenuArea.numberOfMainMenuButtons;
                        }
                    } else if (cmd.equals("select")) {
                        if (selectedButton == 1) {
                            ((MenuArea)area).inDifficultyMenu = true;
                        } else if (selectedButton == 2) {
                            ((MenuArea)area).inHelpMenu = true;
                        } else if (selectedButton == 3) {
                            System.exit(0);
                        }
                    } else if (cmd.equals("quit")) {
                        System.exit(0);
                    }
                }
            }
        }

    }

    public static void createTimers() {

        //Create enemies every 100 milliseconds (every 10th of a second
        enemyCreator = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((player != null  && !((GameArea)area).waitingToStart && !gamePaused) || (area instanceof MenuArea && !backgroundDisabled)) {
                    //Create a new enemy

                    //A random size between the minimum size and maximum size, the enemy is always a square
                    int size = ThreadLocalRandom.current().nextInt(Enemy.sizeBoundaryLower, Enemy.sizeBoundaryUpper);

                    //A random speed (1 or 2)
                    double speed = Math.random() * 1.5 + 0.5;
                    enemies.add(new Enemy(ThreadLocalRandom.current().nextInt(0, (int) Dodger.size.getWidth() - size), (int) Dodger.size.getHeight(), new Dimension(size, size), speed));

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
        enemyMover = new Timer(25, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Move the enemies and check for collisions
                if ((player != null && !((GameArea)area).waitingToStart && !gamePaused) || (area instanceof MenuArea && !backgroundDisabled)) {
                    for (Enemy enemy : enemies) {
                        enemy.move();
                    }
                }
            }
        });

        //Collision detection
        collisionManager = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (player != null  && !((GameArea)area).waitingToStart && !gamePaused) {
                    ArrayList<Enemy> newEnemies = new ArrayList<>();
                    for (Enemy enemy : enemies) {
                        if (player.intersects(enemy)) {
                            if(player.getStoredPowerup() != 3) { //If it is not an armor powerup
                                died();
                            } else {
                                player.setStoredPowerup(-1); //Remove the powerup
                                continue;
                            }
                        }
                        newEnemies.add(enemy);
                    }
                    enemies = newEnemies;
                    //Check to see if they have run into a powerup
                    ArrayList<Powerup> newPowerups = new ArrayList<>();
                    for (Powerup p : powerups) {
                        if (player.getStoredPowerup() != -1) {
                            newPowerups.add(p);
                            continue;
                        }
                        if (player.intersects(p.x, p.y, (int) Powerup.SIZE.getWidth(), (int) Powerup.SIZE.getHeight())) {
                            player.setStoredPowerup(p.type);
                            continue;
                        }
                        newPowerups.add(p);
                    }
                    Dodger.powerups = newPowerups;
                }
            }
        });

        //Get player input every 20 milliseconds
        inputManager = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //In-game input
                if (player != null  && !((GameArea)area).waitingToStart && !gamePaused) {
                    //Vertical movement
                    if (keysHeld.contains("up")) {
                        player.decreaseyVel(0.15);
                    } else if (keysHeld.contains("down")) {
                        player.increaseyVel(0.15);
                    } else {//Slow the player down if no keys are being pressed
                        player.decreaseyVel(player.getyVel() / 15); //Cuts speed to 15%
                    }

                    //Horizontal movement
                    if (keysHeld.contains("left")) {
                        player.decreasexVel(0.15);
                    } else if (keysHeld.contains("right")) {
                        player.increasexVel(0.15);
                    } else { //Slow the player down if no keys are being pressed
                        player.decreasexVel(player.getxVel() / 15); //Cuts speed to 15%
                    }

                    //If the player has run into a wall, stop them
                    if (player.x + player.getxVel() <= 0) {
                        player.x = 0;
                        player.setxVel(0);
                    }
                    if (player.x + player.getSize().getWidth() + player.getxVel() >= size.getWidth()) {
                        player.x = size.getWidth() - player.getSize().getWidth();
                        player.setxVel(0);
                    }
                    if (player.y + player.getyVel() <= 0) {
                        player.y = 0;
                        player.setyVel(0);
                    }
                    if (player.y + player.getSize().getWidth() + player.getyVel() >= size.getHeight()) {
                        player.y = size.getHeight() - player.getSize().getWidth();
                        player.setyVel(0);
                    }
                    //Move the player according to the velocity
                    player.x += player.getxVel();
                    player.y += player.getyVel();
                } else { //Menu area

                }
            }
        });

        //Increase the score every second
        scoreKeeper = new Timer(1000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (player != null && !((GameArea)area).waitingToStart && !gamePaused) {
                    score++;
                }

            }
        });

        //Add a powerup to the screen every 5 seconds
        powerupAdder = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (player != null  && !((GameArea)area).waitingToStart && !gamePaused) {
                    int type = ThreadLocalRandom.current().nextInt(1, Powerup.numberOfpowerUpTypes + 1);
                    //Position
                    int x = ThreadLocalRandom.current().nextInt(0, (int) (Dodger.size.getWidth() - Powerup.SIZE.getWidth()));
                    int y = ThreadLocalRandom.current().nextInt(0, (int) (Dodger.size.getHeight() - Powerup.SIZE.getHeight()));
                    //Create it
                    powerups.add(new Powerup(type, x, y));
                }
            }
        });

        timersCreated = true;
    }
}