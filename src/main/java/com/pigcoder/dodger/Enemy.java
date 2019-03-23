package com.pigcoder.dodger;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Enemy extends Rectangle2D.Double {

    public static final int sizeBoundaryLower = 10;
    public static final int sizeBoundaryUpper = 40;

    private Dimension size;

    //The distance the enemy moves
    private double speed = 1;

    private double xVel;
    private double yVel;

    public double getxVel() { return xVel; }
    public void setxVel(double xVel) { this.xVel = xVel; }
    public double getyVel() { return yVel; }
    public void setyVel(double yVel) { this.yVel = yVel; }

    public Dimension getSize() { return size; }
    public void setSize(Dimension d) { size = d; }

    public double getSpeed() { return speed; }
    public void setSpeed(int s) { speed = s; }

    public void move() {
        y-=speed;
    }

    public Enemy(int x, int y, Dimension size, double speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.width = size.getWidth();
        this.height = size.getHeight();
    }

}