package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
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

        List<UserMealWithExcess> mealsTo_Optional2_var3 = filteredByCyclesOptional2_var3(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo_Optional2_var3.forEach(System.out::println);

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
        //в 1 цикл по исходному списку. Результат заполняется при выполнении задач, созданных в этом цикле, после отпускания lock'а

        Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();
        List<UserMealWithExcess> result = new CopyOnWriteArrayList<>();

        ExecutorService executor = Executors.newCachedThreadPool();

        ReentrantLock lock = new ReentrantLock();
        lock.lock();

        for (UserMeal meal : meals) {
            caloriesPerDays.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime))
                executor.submit(() -> {
                    lock.lock();
                    boolean excess = caloriesPerDays.get(meal.getDateTime().toLocalDate()) > caloriesPerDay;
                    result.add(createUserMealWithExcess(meal, excess));
                    lock.unlock();
                });
        }

        lock.unlock();

        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
                executor.shutdown();
            } catch (InterruptedException ignored) {
            }
        }

        return result;
    }

    public static List<UserMealWithExcess> filteredByCyclesOptional2_var2(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        //в 1 цикл по исходному списку. Результат заполняется при выполнении отложенных задач, созданных в этом цикле

        Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();
        List<Callable<Void>> callables = new ArrayList<>();
        List<UserMealWithExcess> result = Collections.synchronizedList(new ArrayList<>());

        for (UserMeal meal : meals) {
            caloriesPerDays.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime))
                callables.add(() -> {
                    boolean excess = caloriesPerDays.get(meal.getDateTime().toLocalDate()) > caloriesPerDay;
                    result.add(createUserMealWithExcess(meal, excess));
                    return null;
                });
        }

        try {
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.invokeAll(callables);
            executorService.shutdown();
        } catch (InterruptedException ignored) {
        }

        return result;
    }

    public static List<UserMealWithExcess> filteredByCyclesOptional2_var3(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        //в 1 цикл по исходному списку с использованием (добавленных) статического поля и метода класса UserMeal

        List<UserMealWithExcess> result = new ArrayList<>();

        meals.forEach(meal -> {
            if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                boolean excess = UserMeal.getCaloriesPerDay(meal.getDateTime().toLocalDate()) > caloriesPerDay;
                result.add(createUserMealWithExcess(meal, excess));
            }
        });

        return result;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        //в 1 проход по meals через stream()

        return
                meals.stream()
                        .collect(Collectors.groupingBy((meal) -> meal.getDateTime().toLocalDate()))
                        .values()
                        .stream()
                        .flatMap((p) -> {
                            boolean excess = p.stream().mapToInt(UserMeal::getCalories).sum() > caloriesPerDay;
                            return p.stream()
                                    .filter((meal) -> TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime))
                                    .map((meal) -> createUserMealWithExcess(meal, excess));
                        })
                        .collect(Collectors.toList());
    }

    private static UserMealWithExcess createUserMealWithExcess(UserMeal meal, boolean excess) {
        return new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess);
    }
}
