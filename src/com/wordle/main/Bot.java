package com.wordle.main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Bot {
	
	// Based on the possible words, find the word that eliminates the most choices
	private Set<String> allWords;
	private Set<String> possibleWords;
	private int attempts;
	
	private static Status[][] patterns;
	
	// There are 3^5 possible patterns
	private static final int POSSIBLE_PATTERNS = 243;
	
	public Bot(Set<String> allWords) {
		this.allWords = allWords;
		this.possibleWords = allWords;
		attempts = 0;
		patterns = new Status[POSSIBLE_PATTERNS][Game.WORD_LENGTH];
		createPatterns();
	}
	
	public void updatePossibleWords(Status[] pattern, String guess) {
		Set<String> nextWords = new HashSet<>();
		filterWords(nextWords, pattern, guess);
		possibleWords = nextWords;
		attempts++;
	}
	
	/**
	 * Return the bot's guess
	 * @return
	 */
	public String computeWord() {
		// Default word to use given no information
		if (possibleWords.size() == allWords.size()) {
			return "TARES";
		}
		
		// If only one possible word or at point where guess must be made, make guess
		if (possibleWords.size() == 1 || attempts == Game.WORD_COUNT - 1) {
			for (String word : possibleWords) {
				return word;
			}
		}
		
		/* Entropy is the measure of information that selecting a given word has.
		In other words, we measure the amount of entropy by how many words from the
		possible word list the given word will eliminate. The more words from the 
		possible words eliminated, the greater the entropy. */
		
		String bestWord = null;
		double maxEntropy = 0.0;
		
		Worker[] workers = new Worker[allWords.size()];
		Thread[] threads = new Thread[allWords.size()];
		
		int index = 0;
		for (String word : allWords) {
			workers[index] = new Worker(word);
			threads[index] = new Thread(workers[index]);
			threads[index++].start();
		}
		
		index = 0;
		for (Thread thread : threads) {
			try {
				index++;
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (Worker worker : workers) {
			if (Double.compare(worker.entropy, maxEntropy) > 0) {
				maxEntropy = worker.entropy;
				bestWord = worker.word;
			}
		}
		
		return bestWord;
	}
	
	/**
	 * This method updates each possible word could be the target word given
	 * the pattern that was rendered from the guess. If this pattern could have
	 * possibly been generated with the candidate word being the target word,
	 * then this is still a valid word. Otherwise, we eliminate the word from
	 * the list of possible words.
	 * @param wordList
	 * @param pattern
	 * @param guess
	 */
	private void filterWords(Set<String> wordList, Status[] pattern, String guess) {
		for (String candidate : possibleWords) {
			boolean valid = true;
			
			// Get the letter counts for the candidate word
			Map<Character, Integer> letterCount = new HashMap<>();
			for (char letter : candidate.toCharArray()) {
				if (!letterCount.containsKey(letter)) {
					letterCount.put(letter, 1);
				} else {
					letterCount.put(letter, letterCount.get(letter) + 1);
				}
			}
			
			for (int i = 0; valid && i < Game.WORD_LENGTH; i++) {
				Status status = pattern[i];
				char letter = guess.charAt(i);
				
				/* If the current letter is a match, then the candidate is valid
				 * if the candidate letter matches the current letter.
				 */
				if (status == Status.MATCH) {
					valid = (candidate.charAt(i) == letter);
					if (valid) {
						letterCount.put(letter, letterCount.get(letter) - 1);
					}
				}
			}
			
			for (int i = 0; valid && i < Game.WORD_LENGTH; i++) {
				Status status = pattern[i];
				char letter = guess.charAt(i);
				
				/* If the current letter is contained in the target word, then the
				 * candidate is valid if the candidate still contains the letter
				 * elsewhere in the string.
				 */
				if (status == Status.CONTAINS) {
					valid = (letterCount.containsKey(letter) &&
							letterCount.get(letter) > 0 &&
							candidate.charAt(i) != letter);
					if (valid) {
						letterCount.put(letter, letterCount.get(letter) - 1);
					}
				}
				
				/* If the current letter is not contained in the target word, the
				 * candidate is valid if the candidate does not contain it anymore.
				 */
				if (status == Status.NONE) {
					valid = (!letterCount.containsKey(letter) ||
							letterCount.get(letter) == 0);
				}
			}
			
			if (valid) {
				wordList.add(candidate);
			}
		}
	}
	
	private static void createPatterns() {
		createPattern(0, 0, new Status[5]);
	}
	
	/**
	 * Generate all possible patterns that can form for a given word (243 total)
	 * @param count
	 * @param index
	 * @param pattern
	 */
	private static void createPattern(int count, int index, Status[] pattern) {
		if (index == Game.WORD_LENGTH) {
			patterns[count] = Arrays.copyOf(pattern, pattern.length);
		} else {
			int multiplier = POSSIBLE_PATTERNS;
			for (int i = 0; i < index + 1; i++) {
				multiplier /= Status.values().length;
			}
			int i = 0;
			for (Status status : Status.values()) {
				pattern[index] = status;
				createPattern(count + multiplier * i, index + 1, pattern);
				i++;
			}
		}
	}
	
	/**
	 * Creates a runnable instance to process a given candidate word.
	 * Will calculate the entropy for that word.
	 * @author tanim
	 *
	 */
	private class Worker implements Runnable {
		
		private String word;
		private double entropy;
		
		public Worker(String word) {
			this.word = word;
			entropy = 0;
		}
		
		/* For all possible patterns that could form from selecting this word
		 * (based on the candidate words), see how many words would be eliminated
		 * from the current list of possible words. The more words that are
		 * eliminated on average, the stronger the entropy of the word. However,
		 * keep in mind that it also means that the word and pattern combination
		 * is unlikely since it does not match many candidate words.
		 */
		public void run() {
			for (Status[] pattern : patterns) {
				Set<String> nextWords = new HashSet<>();
				filterWords(nextWords, pattern, word);
				
				/* Probability that this guess and pattern would be valid given the 
				 * subset of possible words.
				 */
				double probability = (double) nextWords.size() / possibleWords.size();
				if (probability > 0) {
					// Formula for information is -log2(p) = log2(1/p)
					entropy += probability * (Math.log(1.0 / probability) / Math.log(2));
				}
			}
		}

	}

}
