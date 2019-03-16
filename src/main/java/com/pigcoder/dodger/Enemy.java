package com.pigcoder.dodger;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Enemy {

    public static final int sizeBoundaryLower = 10;
    public static final int sizeBoundaryUpper = 40;

    //Position
    private Point position;

    private Dimension size;

    //The distance the enemy moves
    private int speed = 1;

    private Rectangle2D.Double shape;


    public Point getPosition() { return position; }
    public void setPosition(Point p) { position = p; }

    public Dimension getSize() { return size; }
    public void setSize(Dimension d) { size = d; }

    public int getSpeed() { return speed; }
    public void setSpeed(int s) { speed = s; }

    public Rectangle2D.Double getShape() { return shape; }

    public void move() {
        position.translate(0, -speed);
        shape.y = position.y;
    }

    public Enemy(Point p, Dimension size, int speed) {
        this.position = p;
        this.size = size;
        this.speed = speed;
        shape = new Rectangle2D.Double(p.getX(),p.getY(),size.getWidth(),size.getHeight());
    }

}