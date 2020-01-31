package ru.javawebinar.topjava.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class UserMeal {

    private final static Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();

    private final LocalDateTime dateTime;

    private final String description;

    private final int calories;

    public static int getCaloriesPerDay(LocalDate localDate) {
        return caloriesPerDays.getOrDefault(localDate, 0);
    }

    public UserMeal(LocalDateTime dateTime, String description, int calories) {
        this.dateTime = dateTime;
        this.description = description;
        this.calories = calories;

        caloriesPerDays.merge(this.dateTime.toLocalDate(), this.calories, Integer::sum);
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getDescription() {
        return description;
    }

    public int getCalories() {
        return calories;
    }

}
