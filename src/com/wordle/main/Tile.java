package com.wordle.main;

import java.awt.Color;
import java.awt.Graphics;

public class Tile extends GameObject {
	
	private char letter;
	private Color color;

	public Tile(int x, int y, int width, int height, char letter, 
			Color color, ID id) {
		super(x, y, width, height, id);
		this.letter = letter;
		this.color = color;
	}
	
	public void setLetter(char letter) {
		this.letter = letter;
	}
	
	public char getLetter() {
		return letter;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}


	@Override
	public void render(Graphics g) {
		g.setColor(color);
		g.fillRect(x, y, width, height);
		
		g.setColor(Color.WHITE);
		g.drawRect(x, y, width, height);
		
		if (letter >= 'A' && letter <= 'Z') {
			Game.drawString(g, String.valueOf(letter), x, y, width, height);
		}
	}

}
