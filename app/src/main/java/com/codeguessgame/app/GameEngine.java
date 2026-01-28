package com.codeguessgame.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private String secretCode;
    private List<Guess> guesses;
    private int maxAttempts;
    private int codeLength;
    private boolean isGameWon;
    private boolean isDailyChallenge;
    private Random random;

    public GameEngine(int codeLength, int maxAttempts, boolean isDailyChallenge) {
        this.codeLength = codeLength;
        this.maxAttempts = maxAttempts;
        this.isDailyChallenge = isDailyChallenge;
        this.guesses = new ArrayList<>();
        this.isGameWon = false;
        this.random = new Random();
        generateSecretCode();
    }

    private void generateSecretCode() {
        if (isDailyChallenge) {
            // Use seed based on current date for daily challenge
            long seed = System.currentTimeMillis() / (1000 * 60 * 60 * 24); // Days since epoch
            random.setSeed(seed);
        }
        
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        secretCode = code.toString();
    }

    public GuessResult submitGuess(String guess) {
        if (guess.length() != codeLength || !guess.matches("\\d{" + codeLength + "}")) {
            return null; // Invalid guess
        }

        if (isGameWon || guesses.size() >= maxAttempts) {
            return null; // Game already finished
        }

        int rightPlace = 0;
        int rightNumber = 0;

        // Count right place
        boolean[] secretUsed = new boolean[codeLength];
        boolean[] guessUsed = new boolean[codeLength];

        for (int i = 0; i < codeLength; i++) {
            if (guess.charAt(i) == secretCode.charAt(i)) {
                rightPlace++;
                secretUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        // Count right number wrong place
        for (int i = 0; i < codeLength; i++) {
            if (!guessUsed[i]) {
                for (int j = 0; j < codeLength; j++) {
                    if (!secretUsed[j] && guess.charAt(i) == secretCode.charAt(j)) {
                        rightNumber++;
                        secretUsed[j] = true;
                        break;
                    }
                }
            }
        }

        GuessResult result = new GuessResult(guess, rightPlace, rightNumber);
        guesses.add(new Guess(guess, rightPlace, rightNumber));

        if (rightPlace == codeLength) {
            isGameWon = true;
        }

        return result;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public List<Guess> getGuesses() {
        return new ArrayList<>(guesses);
    }

    public int getRemainingAttempts() {
        return maxAttempts - guesses.size();
    }

    public boolean isGameWon() {
        return isGameWon;
    }

    public boolean isGameOver() {
        return isGameWon || guesses.size() >= maxAttempts;
    }

    public void reset() {
        guesses.clear();
        isGameWon = false;
        generateSecretCode();
    }

    public static class Guess {
        private String guess;
        private int rightPlace;
        private int rightNumber;

        public Guess(String guess, int rightPlace, int rightNumber) {
            this.guess = guess;
            this.rightPlace = rightPlace;
            this.rightNumber = rightNumber;
        }

        public String getGuess() {
            return guess;
        }

        public int getRightPlace() {
            return rightPlace;
        }

        public int getRightNumber() {
            return rightNumber;
        }
    }

    public static class GuessResult {
        private String guess;
        private int rightPlace;
        private int rightNumber;

        public GuessResult(String guess, int rightPlace, int rightNumber) {
            this.guess = guess;
            this.rightPlace = rightPlace;
            this.rightNumber = rightNumber;
        }

        public String getGuess() {
            return guess;
        }

        public int getRightPlace() {
            return rightPlace;
        }

        public int getRightNumber() {
            return rightNumber;
        }
    }
}
