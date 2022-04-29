package com.wordle.main;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.wordle.main.Game.STATE;

public class KeyInput extends KeyAdapter {
	
	private Game game;
	private Handler handler;
	
	public KeyInput(Game game, Handler handler) {
		this.game = game;
		this.handler = handler;
	}
	
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		/* Handle key presses when game is not in listed states. */
		for (int i = 0; !Game.paused && !Game.loading && !Game.error && !Game.calculating &&
				i < handler.object.size(); i++) {
			GameObject tempObject = handler.object.get(i);
			
			if (tempObject.getID() == ID.Board) {
				((Board) tempObject).handleKeyPress(key);
			}
		}
		
		/* Space will pause the game, and another space will unpause. */
		if (game.gameState == STATE.Game && key == KeyEvent.VK_SPACE) {
			Game.paused = !Game.paused;
		}
		
		if (key == KeyEvent.VK_ESCAPE) {
			System.exit(1);
		}
	}

}
