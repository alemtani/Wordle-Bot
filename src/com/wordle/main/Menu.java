package com.wordle.main;

import java.awt.Graphics;

import com.wordle.main.Game.STATE;

public class Menu {
	
	private Game game;
	
	public Menu(Game game) {
		this.game = game;
	}
	
	public void render(Graphics g) {
		if (game.gameState == STATE.Menu) {
			
			Game.drawString(g, "Menu", Game.TITLE_X, Game.TITLE_Y, 
					Game.TITLE_WIDTH, Game.TITLE_HEIGHT);
			
			g.drawRect(Game.MENU_X, Game.TOP_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT);
			Game.drawString(g, "Play", Game.MENU_X, Game.TOP_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT);
			
			g.drawRect(Game.MENU_X, Game.MID_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT);
			Game.drawString(g, "Help", Game.MENU_X, Game.MID_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT);
			
			g.drawRect(Game.MENU_X, Game.BOT_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT);
			Game.drawString(g, "Quit", Game.MENU_X, Game.BOT_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT);
			
		} else if (game.gameState == STATE.Help) {
			
			Game.drawString(g, "Help", Game.TITLE_X, Game.TITLE_Y, 
					Game.TITLE_WIDTH, Game.TITLE_HEIGHT);
			
			Game.drawString(g, "Guess the five-letter word!", 
					Game.TEXT_X, Game.TEXT_Y, Game.TEXT_WIDTH, Game.TEXT_HEIGHT);
			
			g.drawRect(Game.MENU_X, Game.BOT_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT);
			Game.drawString(g, "Back", Game.MENU_X, Game.BOT_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT);
			
		} else if (game.gameState == STATE.Win) {
			
			Game.drawString(g, "You Win!", Game.TITLE_X, Game.TITLE_Y, 
					Game.TITLE_WIDTH, Game.TITLE_HEIGHT);
			
			Game.drawString(g, "You got " + game.getTargetWord() + " in " + 
					game.getAttempts() + " attempt" + (game.getAttempts() > 1 ? "s" : ""), 
					Game.TEXT_X, Game.TEXT_Y, Game.TEXT_WIDTH, Game.TEXT_HEIGHT);
			
			g.drawRect(Game.MENU_X, Game.BOT_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT);
			Game.drawString(g, "Play Again", Game.MENU_X, Game.BOT_Y, 
					Game.OPT_WIDTH, Game.OPT_HEIGHT);
			
		} else {
			
			Game.drawString(g, "You Lose!", Game.TITLE_X, Game.TITLE_Y, 
					Game.TITLE_WIDTH, Game.TITLE_HEIGHT);
			
			Game.drawString(g, "The word was: " + game.getTargetWord(), 
					Game.TEXT_X, Game.TEXT_Y, Game.TEXT_WIDTH, Game.TEXT_HEIGHT);
			
			g.drawRect(Game.MENU_X, Game.BOT_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT);
			Game.drawString(g, "Play Again", Game.MENU_X, Game.BOT_Y, 
					Game.OPT_WIDTH, Game.OPT_HEIGHT);
			
		}
	}

}
