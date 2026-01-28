package com.codeguessgame.app;

public class GameCategory {
    public static final int DIFFICULTY_EASY = 1;
    public static final int DIFFICULTY_MEDIUM = 2;
    public static final int DIFFICULTY_HARD = 3;
    public static final int DIFFICULTY_DAILY = 4;
    public static final int DIFFICULTY_TIME = 5;
    public static final int DIFFICULTY_EXPERT = 6;

    private String name;
    private String description;
    private int backgroundRes;
    private int codeLength;
    private int maxAttempts;
    private int difficulty;

    public GameCategory(String name, String description, int backgroundRes, int codeLength, int maxAttempts, int difficulty) {
        this.name = name;
        this.description = description;
        this.backgroundRes = backgroundRes;
        this.codeLength = codeLength;
        this.maxAttempts = maxAttempts;
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getBackgroundRes() {
        return backgroundRes;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getDifficulty() {
        return difficulty;
    }
}
