package com.pigcoder.dodger;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Powerup extends Rectangle2D.Double {

	public static final Dimension SIZE = new Dimension(20,20);

	public static final int numberOfpowerUpTypes = 2;

	//Types:
	// 1: Brake
	// 2: Speed Boost
	public int type;

	public Powerup(int type, double x, double y) {
		this.type = type;
		this.x = x;
		this.y = y;
	}

}
