package com.wordle.main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.wordle.main.Game.STATE;

public class MouseInput extends MouseAdapter {
	
	private Game game;
	private Handler handler;
	
	public MouseInput(Game game, Handler handler) {
		this.game = game;
		this.handler = handler;
	}
	
	public void mouseClicked(MouseEvent e) {
		int mx = e.getX();
		int my = e.getY();
		
		/* Only listen to mouse events while game not in current states. */
		if (!Game.paused && !Game.loading && !Game.error && !Game.calculating) {
			if (game.gameState == STATE.Menu) {
				if (mouseOver(mx, my, Game.MENU_X, Game.TOP_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT)) {
					game.gameState = STATE.Game;
					game.startGame();
					return;
				}
				
				if (mouseOver(mx, my, Game.MENU_X, Game.MID_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT)) {
					game.gameState = STATE.Help;
					return;
				}
				
				if (mouseOver(mx, my, Game.MENU_X, Game.BOT_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT)) {
					System.exit(1);
				}
			} else if (game.gameState == STATE.Help) {
				if (mouseOver(mx, my, Game.MENU_X, Game.BOT_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT)) {
					game.gameState = STATE.Menu;
					return;
				}
			} else if (game.gameState == STATE.Win || game.gameState == STATE.Lose) {
				if (mouseOver(mx, my, Game.MENU_X, Game.BOT_Y, Game.OPT_WIDTH, Game.OPT_HEIGHT)) {
					game.gameState = STATE.Game;
					game.startGame();
					return;
				}
			} else if (game.gameState == STATE.Game) {
				if (mouseOver(mx, my, Game.RUN_X, Game.RUN_Y, Game.BUTTON_WIDTH, Game.BUTTON_HEIGHT)) {
					for (int i = 0; i < handler.object.size(); i++) {
						GameObject tempObject = handler.object.get(i);
						
						if (tempObject.getID() == ID.Board) {
							Game.calculating = true;
							((Board) tempObject).runBot();
							Game.calculating = false;
						}
					}
					return;
				}
				
				if (mouseOver(mx, my, Game.RESET_X, Game.RESET_Y, 
						Game.BUTTON_WIDTH, Game.BUTTON_HEIGHT)) {
					game.startGame();
					return;
				}
			}
		}
	}
	
	private boolean mouseOver(int mx, int my, int x, int y, 
			int width, int height) {
		return mx > x && mx < x + width && my > y && my < y + height;
	}

}
