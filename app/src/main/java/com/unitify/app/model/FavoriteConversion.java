package com.unitify.app.model;

public class FavoriteConversion {
    private final ConversionCategory category;
    private final String fromUnit;
    private final String toUnit;

    public FavoriteConversion(ConversionCategory category, String fromUnit, String toUnit) {
        this.category = category;
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FavoriteConversion that = (FavoriteConversion) obj;
        return category == that.category &&
                fromUnit.equals(that.fromUnit) &&
                toUnit.equals(that.toUnit);
    }

    @Override
    public int hashCode() {
        return category.hashCode() * 31 + fromUnit.hashCode() * 17 + toUnit.hashCode();
    }
}

