package com.pigcoder.dodger;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Player {

    public static final Dimension defaultSize = new Dimension(10,10);

    //Position
    private Point position;

    private Dimension size = defaultSize;

    //The amount of pixels the player moves
    private int speed = 1;

    private Rectangle2D.Double shape;

    public Point getPosition() { return position; }
    public void setPosition(Point p) { position = p; }

    public Dimension getSize() { return size; }
    public void setSize(Dimension d) { size = d; }

    public int getSpeed() { return speed; }
    public void setSpeed(int s) { speed = s; }

    public Rectangle2D.Double getShape() { return shape; }

    public void move(char direction, int amount) {
        switch(direction) {
            case('w'): // up
                if(position.getY() - amount > 1) {
                    position.translate(0,-amount);
                    shape.y=position.y;
                }
                break;
            case('a'): // left
                if(position.getX() - amount > 1) {
                    position.translate(-amount,0);
                    shape.x=position.x;
                }
                break;
            case('s'): // down
                if(position.getY() + size.getHeight() + amount < Dodger.size.getHeight()) {
                    position.translate(0, amount);
                    shape.y=position.y;
                }
                break;
            case('d'): // right
                if(position.getX() + size.getWidth() + amount < Dodger.size.getWidth()) {
                    position.translate(amount,0);
                    shape.x=position.x;
                }
                break;
        }
    }

    public Player(Point p) {
        position = p;
        shape = new Rectangle2D.Double(p.getX(),p.getY(),size.getWidth(),size.getHeight());
    }

}