package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
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

        List<UserMealWithExcess> mealsTo_Optional2_var1 = filteredByCyclesOptional2Var1(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo_Optional2_var1.forEach(System.out::println);

        List<UserMealWithExcess> mealsTo_Optional2_var2 = filteredByCyclesOptional2Var2(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo_Optional2_var2.forEach(System.out::println);

        List<UserMealWithExcess> mealsTo_Optional2_var3 = filteredByCyclesOptional2Var3(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo_Optional2_var3.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));

        System.out.println(filteredByStreamsVar2(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();
        for (UserMeal meal : meals) {
            caloriesPerDays.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
        }

        List<UserMealWithExcess> result = new ArrayList<>();
        for (UserMeal meal : meals) {
            if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                result.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), caloriesPerDays.get(meal.getDateTime().toLocalDate()) > caloriesPerDay));
            }
        }

        return result;
    }

    public static List<UserMealWithExcess> filteredByCyclesOptional2Var1(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        //в 1 цикл по исходному списку. Результат заполняется при выполнении задач, созданных в этом цикле, после отпускания lock'а

        Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();
        List<UserMealWithExcess> result = new ArrayList<>();

        ExecutorService executor = Executors.newCachedThreadPool();

        ReentrantLock lock = new ReentrantLock();
        lock.lock();

        for (UserMeal meal : meals) {
            caloriesPerDays.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                executor.submit(() -> {
                    lock.lock();
                    boolean excess = caloriesPerDays.get(meal.getDateTime().toLocalDate()) > caloriesPerDay;
                    result.add(createUserMealWithExcess(meal, excess));
                    lock.unlock();
                });
            }
        }

        lock.unlock();

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
        }

        return result;
    }

    public static List<UserMealWithExcess> filteredByCyclesOptional2Var2(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        //в 1 цикл по исходному списку. Результат заполняется при выполнении отложенных задач, созданных в этом цикле

        Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();
        List<Callable<Void>> callables = new ArrayList<>();
        List<UserMealWithExcess> result = Collections.synchronizedList(new ArrayList<>());

        for (UserMeal meal : meals) {
            caloriesPerDays.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                callables.add(() -> {
                    boolean excess = caloriesPerDays.get(meal.getDateTime().toLocalDate()) > caloriesPerDay;
                    result.add(createUserMealWithExcess(meal, excess));
                    return null;
                });
            }
        }

        try {
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.invokeAll(callables);
            executorService.shutdown();
        } catch (InterruptedException ignored) {
        }

        return result;
    }

    public static List<UserMealWithExcess> filteredByCyclesOptional2Var3(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        //в 1 цикл по исходному списку с использованием рекурсии

        List<UserMealWithExcess> result = new ArrayList<>();
        Map<LocalDate, Integer> caloriesPerDays = new HashMap<>();
        Set<Integer> scannedIndexes = new HashSet<>();

        for (int i = 0; i < meals.size(); i++) {
            doRecursiveCall(i, meals, startTime, endTime, caloriesPerDay, scannedIndexes, caloriesPerDays, result);
        }

        return result;
    }

    private static void doRecursiveCall(int i, List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay, Set<Integer> scannedIndexes, Map<LocalDate, Integer> caloriesPerDays, List<UserMealWithExcess> result) {

        if (scannedIndexes.contains(i)) return;

        scannedIndexes.add(i);

        UserMeal meal = meals.get(i);
        caloriesPerDays.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);

        for (int j = i + 1; j < meals.size(); j++) {
            doRecursiveCall(j, meals, startTime, endTime, caloriesPerDay, scannedIndexes, caloriesPerDays, result);
        }

        if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime)) {
            boolean excess = caloriesPerDays.get(meal.getDateTime().toLocalDate()) > caloriesPerDay;
            result.add(createUserMealWithExcess(meal, excess));
        }
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

    public static List<UserMealWithExcess> filteredByStreamsVar2(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        //в 1 проход по meals через stream() с собственным коллектором

        final class FilteredMealsPerDay {
            private int totalCaloriesPerDay;
            private ArrayList<UserMeal> list = new ArrayList<>();
        }

        Supplier<FilteredMealsPerDay> supplier = FilteredMealsPerDay::new;

        BiConsumer<FilteredMealsPerDay, UserMeal> accumulator = ((mealsPerDay, meal) -> {
            mealsPerDay.totalCaloriesPerDay += meal.getCalories();
            if (TimeUtil.isBetweenInclusive(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                mealsPerDay.list.add(meal);
            }
        });

        BinaryOperator<FilteredMealsPerDay> combiner = (mealsPerDay1, mealsPerDay2) -> {
            mealsPerDay1.totalCaloriesPerDay += mealsPerDay2.totalCaloriesPerDay;
            mealsPerDay1.list.addAll(mealsPerDay2.list);
            return mealsPerDay1;
        };

        Function<FilteredMealsPerDay, Stream<UserMealWithExcess>> finisher = mealsPerDay -> {
            boolean excess = mealsPerDay.totalCaloriesPerDay > caloriesPerDay;
            return mealsPerDay.list.stream().map(meal -> createUserMealWithExcess(meal, excess));
        };

        return
                meals
                        .stream()
                        .collect(Collectors.groupingBy(meal -> meal.getDateTime().toLocalDate(), Collector.of(supplier, accumulator, combiner, finisher)))
                        .values()
                        .stream()
                        .flatMap(Function.identity())
                        .collect(Collectors.toList());
    }

    private static UserMealWithExcess createUserMealWithExcess(UserMeal meal, boolean excess) {
        return new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess);
    }
}
