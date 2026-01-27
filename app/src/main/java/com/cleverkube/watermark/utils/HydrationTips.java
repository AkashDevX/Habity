package com.cleverkube.watermark.utils;

import java.util.Random;

public class HydrationTips {
    private static final String[] TIPS = {
        "Drink a glass of water first thing in the morning to kickstart your metabolism.",
        "Keep a water bottle with you throughout the day for easy access.",
        "Drink water before meals to help with portion control and digestion.",
        "If you feel hungry, try drinking water first - sometimes thirst is mistaken for hunger.",
        "Drink water before, during, and after exercise to stay hydrated.",
        "Add lemon, cucumber, or mint to your water for a refreshing twist.",
        "Set reminders on your phone to drink water at regular intervals.",
        "Drink water when you wake up to rehydrate after sleep.",
        "Carry a reusable water bottle to reduce plastic waste and stay hydrated.",
        "Drink water 30 minutes before meals for better digestion.",
        "Monitor your urine color - pale yellow means you're well hydrated.",
        "Drink water when you feel tired - dehydration can cause fatigue.",
        "Keep water at room temperature for easier consumption.",
        "Drink water after consuming caffeine or alcohol to stay balanced.",
        "Track your water intake to build healthy habits.",
        "Drink water slowly throughout the day rather than all at once.",
        "Eat water-rich foods like watermelon, cucumber, and oranges.",
        "Drink water before bed, but not too much to avoid disruptions.",
        "Stay hydrated in hot weather or during physical activity.",
        "Remember: by the time you feel thirsty, you're already dehydrated."
    };
    
    private static final Random random = new Random();
    
    public static String getRandomTip() {
        return TIPS[random.nextInt(TIPS.length)];
    }
    
    public static String getTipOfTheDay() {
        long dayOfYear = java.time.LocalDate.now().getDayOfYear();
        return TIPS[(int)(dayOfYear % TIPS.length)];
    }
}


