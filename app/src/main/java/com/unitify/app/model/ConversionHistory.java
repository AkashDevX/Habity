package com.unitify.app.model;

import java.util.Date;

public class ConversionHistory {
    private final ConversionCategory category;
    private final String fromUnit;
    private final String toUnit;
    private final double inputValue;
    private final double outputValue;
    private final long timestamp;

    public ConversionHistory(ConversionCategory category, String fromUnit, String toUnit,
                            double inputValue, double outputValue, long timestamp) {
        this.category = category;
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
        this.inputValue = inputValue;
        this.outputValue = outputValue;
        this.timestamp = timestamp;
    }

    public ConversionCategory getCategory() {
        return category;
    }

    public String getFromUnit() {
        return fromUnit;
    }

    public String getToUnit() {
        return toUnit;
    }

    public double getInputValue() {
        return inputValue;
    }

    public double getOutputValue() {
        return outputValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Date getDate() {
        return new Date(timestamp);
    }
}

