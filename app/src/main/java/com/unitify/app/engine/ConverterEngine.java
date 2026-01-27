package com.unitify.app.engine;

import com.unitify.app.model.ConversionCategory;
import com.unitify.app.model.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConverterEngine {
    private static final Map<ConversionCategory, List<Unit>> UNITS = new HashMap<>();
    private static final Map<ConversionCategory, String> BASE_UNITS = new HashMap<>();

    static {
        initializeUnits();
    }

    private static void initializeUnits() {
        // Length (base: meter)
        List<Unit> lengthUnits = new ArrayList<>();
        lengthUnits.add(new Unit("Millimeter", "mm", 0.001));
        lengthUnits.add(new Unit("Centimeter", "cm", 0.01));
        lengthUnits.add(new Unit("Meter", "m", 1.0));
        lengthUnits.add(new Unit("Kilometer", "km", 1000.0));
        lengthUnits.add(new Unit("Inch", "in", 0.0254));
        lengthUnits.add(new Unit("Foot", "ft", 0.3048));
        lengthUnits.add(new Unit("Yard", "yd", 0.9144));
        lengthUnits.add(new Unit("Mile", "mi", 1609.344));
        UNITS.put(ConversionCategory.LENGTH, lengthUnits);
        BASE_UNITS.put(ConversionCategory.LENGTH, "m");

        // Weight (base: kilogram)
        List<Unit> weightUnits = new ArrayList<>();
        weightUnits.add(new Unit("Milligram", "mg", 0.000001));
        weightUnits.add(new Unit("Gram", "g", 0.001));
        weightUnits.add(new Unit("Kilogram", "kg", 1.0));
        weightUnits.add(new Unit("Metric Ton", "t", 1000.0));
        weightUnits.add(new Unit("Ounce", "oz", 0.0283495));
        weightUnits.add(new Unit("Pound", "lb", 0.453592));
        UNITS.put(ConversionCategory.WEIGHT, weightUnits);
        BASE_UNITS.put(ConversionCategory.WEIGHT, "kg");

        // Temperature (special handling)
        List<Unit> tempUnits = new ArrayList<>();
        tempUnits.add(new Unit("Celsius", "°C", 0.0)); // Special
        tempUnits.add(new Unit("Fahrenheit", "°F", 0.0)); // Special
        tempUnits.add(new Unit("Kelvin", "K", 0.0)); // Special
        UNITS.put(ConversionCategory.TEMPERATURE, tempUnits);
        BASE_UNITS.put(ConversionCategory.TEMPERATURE, "K");

        // Area (base: square meter)
        List<Unit> areaUnits = new ArrayList<>();
        areaUnits.add(new Unit("Square Millimeter", "mm²", 0.000001));
        areaUnits.add(new Unit("Square Centimeter", "cm²", 0.0001));
        areaUnits.add(new Unit("Square Meter", "m²", 1.0));
        areaUnits.add(new Unit("Square Kilometer", "km²", 1000000.0));
        areaUnits.add(new Unit("Acre", "ac", 4046.86));
        areaUnits.add(new Unit("Hectare", "ha", 10000.0));
        UNITS.put(ConversionCategory.AREA, areaUnits);
        BASE_UNITS.put(ConversionCategory.AREA, "m²");

        // Volume (base: liter)
        List<Unit> volumeUnits = new ArrayList<>();
        volumeUnits.add(new Unit("Milliliter", "ml", 0.001));
        volumeUnits.add(new Unit("Liter", "l", 1.0));
        volumeUnits.add(new Unit("Cubic Meter", "m³", 1000.0));
        volumeUnits.add(new Unit("Cubic Inch", "in³", 0.0163871));
        volumeUnits.add(new Unit("Cubic Foot", "ft³", 28.3168));
        volumeUnits.add(new Unit("US Gallon", "gal", 3.78541));
        UNITS.put(ConversionCategory.VOLUME, volumeUnits);
        BASE_UNITS.put(ConversionCategory.VOLUME, "l");

        // Speed (base: m/s)
        List<Unit> speedUnits = new ArrayList<>();
        speedUnits.add(new Unit("Meter per Second", "m/s", 1.0));
        speedUnits.add(new Unit("Kilometer per Hour", "km/h", 0.277778));
        speedUnits.add(new Unit("Mile per Hour", "mph", 0.44704));
        speedUnits.add(new Unit("Knot", "kn", 0.514444));
        UNITS.put(ConversionCategory.SPEED, speedUnits);
        BASE_UNITS.put(ConversionCategory.SPEED, "m/s");

        // Time (base: second)
        List<Unit> timeUnits = new ArrayList<>();
        timeUnits.add(new Unit("Millisecond", "ms", 0.001));
        timeUnits.add(new Unit("Second", "s", 1.0));
        timeUnits.add(new Unit("Minute", "min", 60.0));
        timeUnits.add(new Unit("Hour", "h", 3600.0));
        timeUnits.add(new Unit("Day", "d", 86400.0));
        UNITS.put(ConversionCategory.TIME, timeUnits);
        BASE_UNITS.put(ConversionCategory.TIME, "s");

        // Data (base: byte, 1024-based)
        List<Unit> dataUnits = new ArrayList<>();
        dataUnits.add(new Unit("Bit", "bit", 0.125));
        dataUnits.add(new Unit("Byte", "B", 1.0));
        dataUnits.add(new Unit("Kilobyte", "KB", 1024.0));
        dataUnits.add(new Unit("Megabyte", "MB", 1048576.0));
        dataUnits.add(new Unit("Gigabyte", "GB", 1073741824.0));
        dataUnits.add(new Unit("Terabyte", "TB", 1099511627776.0));
        UNITS.put(ConversionCategory.DATA, dataUnits);
        BASE_UNITS.put(ConversionCategory.DATA, "B");

        // Energy (base: joule)
        List<Unit> energyUnits = new ArrayList<>();
        energyUnits.add(new Unit("Joule", "J", 1.0));
        energyUnits.add(new Unit("Kilojoule", "kJ", 1000.0));
        energyUnits.add(new Unit("Calorie", "cal", 4.184));
        energyUnits.add(new Unit("Kilocalorie", "kcal", 4184.0));
        energyUnits.add(new Unit("Watt Hour", "Wh", 3600.0));
        energyUnits.add(new Unit("Kilowatt Hour", "kWh", 3600000.0));
        UNITS.put(ConversionCategory.ENERGY, energyUnits);
        BASE_UNITS.put(ConversionCategory.ENERGY, "J");

        // Pressure (base: pascal)
        List<Unit> pressureUnits = new ArrayList<>();
        pressureUnits.add(new Unit("Pascal", "Pa", 1.0));
        pressureUnits.add(new Unit("Kilopascal", "kPa", 1000.0));
        pressureUnits.add(new Unit("Bar", "bar", 100000.0));
        pressureUnits.add(new Unit("PSI", "psi", 6894.76));
        pressureUnits.add(new Unit("Atmosphere", "atm", 101325.0));
        UNITS.put(ConversionCategory.PRESSURE, pressureUnits);
        BASE_UNITS.put(ConversionCategory.PRESSURE, "Pa");
    }

    public static List<Unit> getUnits(ConversionCategory category) {
        return new ArrayList<>(UNITS.get(category));
    }

    public static Unit findUnit(ConversionCategory category, String unitName) {
        List<Unit> units = UNITS.get(category);
        if (units == null) return null;
        for (Unit unit : units) {
            if (unit.getName().equals(unitName) || unit.getSymbol().equals(unitName)) {
                return unit;
            }
        }
        return null;
    }

    public static double convert(ConversionCategory category, String fromUnitName, String toUnitName, double value) {
        if (category == ConversionCategory.TEMPERATURE) {
            return convertTemperature(fromUnitName, toUnitName, value);
        }

        Unit fromUnit = findUnit(category, fromUnitName);
        Unit toUnit = findUnit(category, toUnitName);

        if (fromUnit == null || toUnit == null) {
            return 0.0;
        }

        // Convert to base unit, then to target unit
        double valueInBase = value * fromUnit.getFactorToBase();
        return valueInBase / toUnit.getFactorToBase();
    }

    private static double convertTemperature(String fromUnit, String toUnit, double value) {
        // Convert to Kelvin first
        double kelvin;
        if (fromUnit.equals("Celsius") || fromUnit.equals("°C")) {
            kelvin = value + 273.15;
        } else if (fromUnit.equals("Fahrenheit") || fromUnit.equals("°F")) {
            kelvin = (value - 32) * 5.0 / 9.0 + 273.15;
        } else if (fromUnit.equals("Kelvin") || fromUnit.equals("K")) {
            kelvin = value;
        } else {
            return 0.0;
        }

        // Convert from Kelvin to target
        if (toUnit.equals("Celsius") || toUnit.equals("°C")) {
            return kelvin - 273.15;
        } else if (toUnit.equals("Fahrenheit") || toUnit.equals("°F")) {
            return (kelvin - 273.15) * 9.0 / 5.0 + 32;
        } else if (toUnit.equals("Kelvin") || toUnit.equals("K")) {
            return kelvin;
        } else {
            return 0.0;
        }
    }
}

