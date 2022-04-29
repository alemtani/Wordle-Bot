package com.wordle.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.wordle.main.Game.STATE;

public class Board extends GameObject {
	
	private Game game;
	private Tile[][] tiles;
	private int currX, currY;
	
	// Store logic of game
	
	private String targetWord;
	private Set<String> allWords;
	private Map<Character, Integer> targetLetters;
	
	private Bot bot;

	public Board(int x, int y, int width, int height, Game game,
			Tile[][] tiles, String targetWord, Set<String> allWords, ID id) {
		super(x, y, width, height, id);
		this.game = game;
		this.tiles = tiles;
		this.targetWord = targetWord;
		this.allWords = allWords;
		
		currX = 0;
		currY = 0;
		
		// Track target letters for in-game hints
		
		targetLetters = new HashMap<Character, Integer>();
		for (int i = 0; i < Game.WORD_LENGTH; i++) {
			char letter = targetWord.charAt(i);
			
			if (!targetLetters.containsKey(letter)) {
				targetLetters.put(letter, 0);
			}
			
			targetLetters.put(letter, targetLetters.get(letter) + 1);
		}
		
		bot = new Bot(allWords);
	}

	@Override
	public void render(Graphics g) {
		// TODO Auto-generated method stub
	}
	
	public void handleKeyPress(int key) {
		if (game.gameState == STATE.Game) {
			// Allow inputting letters into tiles to create word
			if (key >= KeyEvent.VK_A && key <= KeyEvent.VK_Z) {
				char letter = (char) ('A' + (key - KeyEvent.VK_A));
				
				if (currX < Game.WORD_LENGTH) {
					tiles[currY][currX++].setLetter(letter);
				}
			}
			
			if (key == KeyEvent.VK_ENTER) {
				// If a five-length string, perform processing
				if (currX == Game.WORD_LENGTH) {
					StringBuilder currWord = new StringBuilder();
					
					for (int i = 0; i < Game.WORD_LENGTH; i++) {
						currWord.append(tiles[currY][i].getLetter());
					}
					
					String guess = currWord.toString();
					
					if (allWords.contains(guess)) { // Must be valid word in list to submit
						submit(guess);
					} else { // Inform player that word is invalid
						Game.error = true;
						
						long startTime = System.currentTimeMillis(),
								currentTime = System.currentTimeMillis();
						
						while (currentTime - startTime < 2000) {
							currentTime = System.currentTimeMillis();
						}
						
						Game.error = false;
					}
				}
			}
			
			if (key == KeyEvent.VK_BACK_SPACE) {
				if (currX > 0) {
					tiles[currY][--currX].setLetter(Character.MIN_VALUE);
				}
			}
		}
	}
	
	/**
	 * This method processes the submitted word to inform whether correct or not
	 * @param guess
	 */
	private void submit(String guess) {
		Map<Character, Integer> lettersCopy = new HashMap<>(targetLetters);
		Status[] pattern = new Status[Game.WORD_LENGTH];
		
		boolean foundWord = true;
		
		for (int i = 0; i < Game.WORD_LENGTH; i++) {
			char currLetter = guess.charAt(i);
			char targetLetter = targetWord.charAt(i);
			
			if (currLetter == targetLetter) {
				pattern[i] = Status.MATCH;
				lettersCopy.put(currLetter, lettersCopy.get(currLetter) - 1);
			} else { // At this point, impossible this is the word
				foundWord = false;
				pattern[i] = Status.NONE;
			}
		}
		
		for (int i = 0; i < Game.WORD_LENGTH; i++) {
			char currLetter = guess.charAt(i);
			char targetLetter = targetWord.charAt(i);
			
			if (currLetter != targetLetter &&
					lettersCopy.containsKey(currLetter) && 
					lettersCopy.get(currLetter) > 0) {
				// Essentially, still contains letter but just not in right position
				pattern[i] = Status.CONTAINS;
				lettersCopy.put(currLetter, lettersCopy.get(currLetter) - 1);
			}
		}
		
		Game.loading = true;
		
		for (int i = 0; i < Game.WORD_LENGTH; i++) {
			long startTime = System.currentTimeMillis(),
					currentTime = System.currentTimeMillis();
			
			Color color = null;
			
			switch (pattern[i]) {
			case NONE:
				color = Color.GRAY;
				break;
			case CONTAINS:
				color = Color.YELLOW;
				break;
			default:
				color = Color.GREEN;
				break;
			}
			
			tiles[currY][i].setColor(color);
			
			while (currentTime - startTime < 200) {
				currentTime = System.currentTimeMillis();
			}
		}
		
		Game.loading = false;
		
		currY++;
		currX = 0;
		
		if (foundWord) {
			game.gameState = STATE.Win;
			game.setAttempts(currY);
		} else if (currY == Game.WORD_COUNT) { // Word not found and all attempts used
			game.gameState = STATE.Lose;
			game.setAttempts(currY);
		} else { // Allows bot to continue to find word
			bot.updatePossibleWords(pattern, guess);
		}
	}
	
	/**
	 * Set the word to the bot's choice.
	 */
	public void runBot() {
		if (game.gameState == STATE.Game) {
			String guess = bot.computeWord();
			
			for (int i = 0; i < Game.WORD_LENGTH; i++) {
				tiles[currY][i].setLetter(guess.charAt(i));
			}
			
			currX = Game.WORD_LENGTH;
		}
	}

}
