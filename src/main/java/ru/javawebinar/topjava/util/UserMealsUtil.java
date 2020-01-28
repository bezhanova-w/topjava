package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410));

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();
        //List<UserMeal> listOfFilteredMeals = new ArrayList<UserMeal>();
        Map<LocalDate, List<UserMeal>> filteredMeals = new HashMap<>();

        for (UserMeal meal : meals) {
            LocalDate ld = meal.getDateTime().toLocalDate();
            caloriesPerDays.merge(ld, meal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime))
                filteredMeals.merge(ld, Arrays.asList(meal), (oldVal, newVal) -> {oldVal.addAll(newVal); return oldVal;});
                //listOfFilteredMeals.add(meal);
        }

        List<UserMealWithExcess> filteredMealsWithExcess = new ArrayList<>();
        boolean excess = false;

        /*
        for (UserMeal meal : listOfFilteredMeals) {
            excess = caloriesPerDays.getOrDefault(meal.getDateTime().toLocalDate(), 0) > caloriesPerDay;
            filteredMealsWithExcess.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess));
        }
        */

        for (Map.Entry<LocalDate, List<UserMeal>> entry : filteredMeals.entrySet()) {
            excess = caloriesPerDays.get(entry.getKey()) > caloriesPerDay;
            for (UserMeal meal : entry.getValue()) {
                filteredMealsWithExcess.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess));
            }
        }

        return filteredMealsWithExcess;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        return
        meals.stream()
                .collect(Collectors.groupingBy((meal) ->  meal.getDateTime().toLocalDate()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap((p) -> p.getValue().stream().mapToInt(UserMeal :: getCalories).sum() > caloriesPerDay,
                        (p) -> p.getValue()
                                .stream()
                                .filter((meal) -> TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime))
                                .collect(Collectors.toList()),
                        (oldVal, newVal) -> {oldVal.addAll(newVal); return oldVal;}))
                .entrySet()
                .stream()
                .flatMap((p) -> p.getValue().stream().map((meal) -> new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), p.getKey())))
                .collect(Collectors.toList());
    }
}
