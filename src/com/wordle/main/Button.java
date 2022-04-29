package com.wordle.main;

import java.awt.Color;
import java.awt.Graphics;

public class Button extends GameObject {
	
	private String text;

	public Button(int x, int y, int width, int height, String text, ID id) {
		super(x, y, width, height, id);
		this.text = text;
	}

	@Override
	public void render(Graphics g) {
		// TODO Auto-generated method stub
		g.setColor(Color.WHITE);
		g.drawRect(x, y, width, height);
		Game.drawString(g, text, x, y, width, height);
	}

}
