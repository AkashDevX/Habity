package com.unitify.app.model;

public class Unit {
    private final String name;
    private final String symbol;
    private final double factorToBase; // Factor to convert to base unit

    public Unit(String name, String symbol, double factorToBase) {
        this.name = name;
        this.symbol = symbol;
        this.factorToBase = factorToBase;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getFactorToBase() {
        return factorToBase;
    }

    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
}

