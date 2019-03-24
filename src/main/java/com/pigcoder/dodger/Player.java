package com.pigcoder.dodger;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Player extends Rectangle2D.Double {

    public static final Dimension defaultSize = new Dimension(10,10);

    private Dimension size = defaultSize;

    private boolean hasBrake = false;

    private double xVel;
    private double yVel;

    public double getxVel() { return xVel; }
    public void setxVel(double xVel) { this.xVel = xVel; }
    public void increasexVel(double a) { xVel+=a; }
    public void decreasexVel(double a) { xVel-=a; }
    public double getyVel() { return yVel; }
    public void setyVel(double yVel) { this.yVel = yVel; }
    public void increaseyVel(double a) { yVel+=a; }
    public void decreaseyVel(double a) { yVel-=a; }

    public boolean getHasBrake() { return hasBrake;}
    public void setHasBrake(boolean b) { hasBrake = b;}

    public Dimension getSize() { return size; }
    public void setSize(Dimension d) { size = d; }

    public void move(int direction, double amount) {
        switch(direction) {
            case 1: //up
                y-=amount;
                break;
            case 2: //down
                y+=amount;
                break;
            case 3: //left
                x-=amount;
                break;
            case 4: //right
                x+=amount;
                break;
        }
    }

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = size.getWidth();
        this.height = size.getHeight();
    }
}