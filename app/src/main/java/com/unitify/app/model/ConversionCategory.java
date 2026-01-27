package com.unitify.app.model;

public enum ConversionCategory {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temperature"),
    AREA("Area"),
    VOLUME("Volume"),
    SPEED("Speed"),
    TIME("Time"),
    DATA("Data"),
    ENERGY("Energy"),
    PRESSURE("Pressure");

    private final String displayName;

    ConversionCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

