package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

        List<UserMealWithExcess> mealsTo_Optional2_var1 = filteredByCyclesOptional2_var1(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo_Optional2_var1.forEach(System.out::println);

        List<UserMealWithExcess> mealsTo_Optional2_var2 = filteredByCyclesOptional2_var2(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo_Optional2_var2.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();
        for (UserMeal meal : meals) {
            caloriesPerDays.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
        }

        List<UserMealWithExcess> result = new ArrayList<>();
        for (UserMeal meal : meals) {
            if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime))
                result.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), caloriesPerDays.get(meal.getDateTime().toLocalDate()) > caloriesPerDay));
        }

        return result;
    }

    public static List<UserMealWithExcess> filteredByCyclesOptional2_var1(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();
        List<UserMealWithExcess> result = new ArrayList<>();

        for (UserMeal meal : meals) {
            caloriesPerDays.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime))
                result.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), false));
        }

        try {
            Field fieldExcess = UserMealWithExcess.class.getDeclaredField("excess");
            fieldExcess.setAccessible(true);

            Field fieldDateTime = UserMealWithExcess.class.getDeclaredField("dateTime");
            fieldDateTime.setAccessible(true);

            for (UserMealWithExcess mealWithExcess : result) {
                if (caloriesPerDays.get(((LocalDateTime) fieldDateTime.get(mealWithExcess)).toLocalDate()) > caloriesPerDay)
                    fieldExcess.setBoolean(mealWithExcess, true);
            }
        }
        catch (IllegalAccessException | NoSuchFieldException ignored) {}

        return result;
    }

    public static List<UserMealWithExcess> filteredByCyclesOptional2_var2(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();
        List<Callable<Void>> callables = new ArrayList<>();
        List<UserMealWithExcess> result = Collections.synchronizedList(new ArrayList<>());

        for (UserMeal meal : meals) {
            caloriesPerDays.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime))
                callables.add(() -> {result.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), caloriesPerDays.get(meal.getDateTime().toLocalDate()) > caloriesPerDay));
                                     return null;});
        }

        try {
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.invokeAll(callables);
            executorService.shutdown();
        }
        catch (InterruptedException ignored) {}

        return result;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        return
        meals.stream()
                .collect(Collectors.groupingBy((meal) ->  meal.getDateTime().toLocalDate()))
                .values()
                .stream()
                .flatMap((p) -> {boolean excess = p.stream().mapToInt(UserMeal::getCalories).sum() > caloriesPerDay;
                                 return p.stream()
                                         .filter((meal) -> TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime))
                                         .map((meal) -> new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess));})
                .collect(Collectors.toList());
    }
}
