package com.wordle.main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Bot {
	
	// Based on the possible words, find the word that eliminates the most choices
	private Map<String, Map<Character, Integer>> allWords;
	private Set<String> possibleWords;
	private int attempts;
	
	private static Status[][] patterns;
	
	// There are 3^5 possible patterns
	private static final int POSSIBLE_PATTERNS = 243;
	
	public Bot(Map<String, Map<Character, Integer>> allWords) {
		this.allWords = allWords;
		this.possibleWords = new HashSet<>(allWords.keySet());
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
		// Default word to use given no information is TARES
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
		
		WordProcessor[] wordProcessors = new WordProcessor[allWords.size()];
		
		int index = 0;
		for (String word : allWords.keySet()) {
			wordProcessors[index] = new WordProcessor(word);
			wordProcessors[index++].start();
		}
		
		for (WordProcessor wordProcessor : wordProcessors) {
			try {
				wordProcessor.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (WordProcessor wordProcessor : wordProcessors) {
			if (Double.compare(wordProcessor.entropy, maxEntropy) > 0) {
				maxEntropy = wordProcessor.entropy;
				bestWord = wordProcessor.word;
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
	 * Creates a thread instance to process a given candidate word.
	 * Will calculate the entropy for that word.
	 * @author tanim
	 *
	 */
	private class WordProcessor extends Thread {
		
		private String word;
		private double entropy;
		private int[] counts = new int[POSSIBLE_PATTERNS];
		
		public WordProcessor(String word) {
			this.word = word;
			entropy = 0;
			counts = new int[POSSIBLE_PATTERNS];
		}
		
		/* For all possible words, determine the pattern that would be generated
		 * if each possible word was the target word. If a pattern would be generated
		 * by more candidate words, the pattern is more probable. From there we can
		 * calculate the entropy of the pattern.
		 */
		public void run() {
			// For all possible candidates, get the pattern generated
			for (String candidate : possibleWords) {				
				Map<Character, Integer> lettersCopy = new HashMap<>(allWords.get(candidate));
				Status[] pattern = new Status[Game.WORD_LENGTH];
				
				// Create the pattern that would be generated if this were the word
				
				for (int i = 0; i < Game.WORD_LENGTH; i++) {
					char currLetter = word.charAt(i);
					char candidateLetter = candidate.charAt(i);
					
					if (currLetter == candidateLetter) {
						pattern[i] = Status.MATCH;
						lettersCopy.put(currLetter, lettersCopy.get(currLetter) - 1);
					} else { // At this point, impossible this is the word
						pattern[i] = Status.NONE;
					}
				}
				
				for (int i = 0; i < Game.WORD_LENGTH; i++) {
					char currLetter = word.charAt(i);
					char candidateLetter = candidate.charAt(i);
					
					if (currLetter != candidateLetter &&
							lettersCopy.containsKey(currLetter) && 
							lettersCopy.get(currLetter) > 0) {
						// Essentially, still contains letter but just not in right position
						pattern[i] = Status.CONTAINS;
						lettersCopy.put(currLetter, lettersCopy.get(currLetter) - 1);
					}
				}
				
				// Get the index of the pattern
				int index = 0;
				for (int i = Game.WORD_LENGTH - 1; i >= 0; i--) {
					index *= 3;
					Status status = pattern[i];
					
					if (status == Status.CONTAINS) {
						index++;
					}
					
					if (status == Status.MATCH) {
						index += 2;
					}
				}
				
				counts[index]++;
			}
						
			// Get the entropy of each possible pattern
			for (int i = 0; i < POSSIBLE_PATTERNS; i++) {
				if (counts[i] > 0) {
					double probability = (double) counts[i] / possibleWords.size();
					
					// Formula for information is -log2(p) = log2(1/p)
					entropy += probability * (Math.log(1.0 / probability) / Math.log(2));
				}
			}
		}

	}

}
