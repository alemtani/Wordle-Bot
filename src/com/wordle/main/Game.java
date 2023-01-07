package com.wordle.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Game extends Canvas implements Runnable {
	
	private static final long serialVersionUID = 1L;
	
	// Set dimensions of board
	public static final int WIDTH = 480, HEIGHT = 640;
	
	public static final int WORD_COUNT = 6;
	public static final int WORD_LENGTH = 5;
	
	public static final int TILE_SIZE = 80;
	
	// Set position for the board
	public static final int BOARD_X = 40, BOARD_Y = 40;
	
	// Set position for the various buttons
	public static final int RUN_X = 60, RUN_Y = 540;
	public static final int RESET_X = 260, RESET_Y = 540;
	public static final int BUTTON_WIDTH = 160, BUTTON_HEIGHT = 40;
	
	// Set position for a header
	public static final int TITLE_X = 80, TITLE_Y = 40, TITLE_WIDTH = 320, TITLE_HEIGHT = 80;
	
	// Set position for the buttons on the menu
	public static final int MENU_X = 120;
	public static final int TOP_Y = 180, MID_Y = 300, BOT_Y = 420;
	public static final int OPT_WIDTH = 240, OPT_HEIGHT = 60;
	
	// Set position for the text descriptions
	public static final int TEXT_X = 120, TEXT_Y = 240, TEXT_WIDTH = 240, TEXT_HEIGHT = 60;
	
	// Static variables store state of game to update on events
	public static boolean paused = false;
	public static boolean loading = false;
	public static boolean error = false;
	public static boolean calculating = false;
	
	private Thread thread;
	private boolean running = false;
	
	private Handler handler;
	private Menu menu;
	
	private int attempts;
	private String targetWord;
	private Map<String, Map<Character, Integer>> allWords;
	
	// Store possible states of the game
	public enum STATE {
		Menu,
		Help,
		Game,
		Win,
		Lose
	};
	
	public STATE gameState = STATE.Menu;
	
	public Game() {
		handler = new Handler();
		menu = new Menu(this);
		
		this.addKeyListener(new KeyInput(this, handler));
		this.addMouseListener(new MouseInput(this, handler));
		
		new Window(WIDTH, HEIGHT, "Wordle", this);
	}
	
	public synchronized void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}
	
	public synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		this.requestFocus();
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			
			while (delta >= 1) {
				delta--;
			}
			
			if (running) {
				render();
			}
			
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
			}
		}
		
		stop();
	}
	
	private void render() {
		BufferStrategy strat = this.getBufferStrategy();
		
		if (strat == null) {
			this.createBufferStrategy(3);
			return;
		}
		
		Graphics g = strat.getDrawGraphics();
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		if (gameState != STATE.Game) {
			menu.render(g);
		} else {
			handler.render(g);
		}
		
		if (paused) {
			g.setColor(Color.YELLOW);
			drawString(g, "PAUSED", TITLE_X, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT);
		}
		
		if (error) {
			g.setColor(Color.YELLOW);
			drawString(g, "WORD NOT FOUND", TITLE_X, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT);
		}
		
		if (calculating) {
			g.setColor(Color.YELLOW);
			drawString(g, "RUNNING BOT", TITLE_X, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT);
		}
		
		g.dispose();
		strat.show();
	}
	
	private void generateTargetWord() {
		try {
			// Collect the list of words and store in map of word to counts
			
			Scanner input = new Scanner(new File("res/words.txt"));
			
			List<String> orderedWords = new ArrayList<>();
			allWords = new HashMap<>();
			
			while (input.hasNext()) {
				String word = input.next();
				word = word.toUpperCase();
				
				orderedWords.add(word);
				allWords.put(word, new HashMap<>());
				
				// Store the letter counts for each word
				for (int i = 0; i < Game.WORD_LENGTH; i++) {
					char letter = word.charAt(i);
					
					if (!allWords.get(word).containsKey(letter)) {
						allWords.get(word).put(letter, 0);
					}
					
					allWords.get(word).put(
						letter, allWords.get(word).get(letter) + 1);
				}
			}
			
			// Generate random words
			Random random = new Random();
			int index = random.nextInt(orderedWords.size());
			targetWord = orderedWords.get(index);
		} catch (FileNotFoundException e) {
			System.exit(1);
		}
	}
	
	public void startGame() {
		handler.clearObject();
		
		paused = false;
		loading = false;
		error = false;
		calculating = false;
		
		attempts = 0;
		
		generateTargetWord();
		
		Tile[][] tiles = new Tile[WORD_COUNT][WORD_LENGTH];
		
		for (int i = 0; i < WORD_COUNT; i++) {
			for (int j = 0; j < WORD_LENGTH; j++) {
				Tile tile = new Tile(BOARD_X + TILE_SIZE * j, BOARD_Y + TILE_SIZE * i, 
						TILE_SIZE, TILE_SIZE, Character.MIN_VALUE, 
						Color.BLACK, ID.Tile);
				tiles[i][j] = tile;
				handler.addObject(tile);
			}
		}
		
		handler.addObject(new Board(BOARD_X, BOARD_Y, 
				TILE_SIZE * WORD_LENGTH, TILE_SIZE * WORD_COUNT, this,
				tiles, targetWord, allWords, ID.Board));
		handler.addObject(new Button(RUN_X, RUN_Y, BUTTON_WIDTH, BUTTON_HEIGHT, 
				"Run Bot", ID.Run));
		handler.addObject(new Button(RESET_X, RESET_Y, BUTTON_WIDTH, BUTTON_HEIGHT, 
				"Reset", ID.Reset));
	}
	
	public String getTargetWord() {
		return targetWord;
	}
	
	public int getAttempts() {
		return attempts;
	}
	
	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}
	
	/**
	 * This method centers text at some point on the screen given the following parameters:
	 * @param g
	 * @param text
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public static void drawString(Graphics g, String text, 
			int x, int y, int width, int height) {
		Font font = new Font("Arial", Font.BOLD, height / 2);
		g.setColor(Color.WHITE);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics(font);
		int textX = x + ((width - fm.stringWidth(text)) / 2);
		int textY = y + (((height - fm.getHeight()) / 2) + fm.getAscent());
		g.drawString(text, textX, textY);
	}
	
	public static void main(String[] args) {
		new Game();
	}
	
}
