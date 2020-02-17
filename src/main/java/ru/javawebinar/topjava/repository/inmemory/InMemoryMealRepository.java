package ru.javawebinar.topjava.repository.inmemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.DateTimeUtil;
import ru.javawebinar.topjava.util.MealsUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class InMemoryMealRepository implements MealRepository {
    private static final Logger log = LoggerFactory.getLogger(InMemoryMealRepository.class);

    private final Map<Integer, Map<Integer, Meal>> repository = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger(0);

    {
        MealsUtil.MEALS.forEach(meal -> save(meal, InMemoryUserRepository.USER_ID_1));

        Arrays.asList(
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 29, 7, 0), "Завтрак", 600),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 29, 12, 0), "Обед", 1000),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 29, 18, 0), "Ужин", 500))
                .forEach(meal -> save(meal, InMemoryUserRepository.USER_ID_2));
    }

    @Override
    public Meal save(Meal meal, int userId) {
        log.info("save meal {} for user {}", meal, userId);
        Map<Integer, Meal> mealsMap = repository.computeIfAbsent(userId, key -> new ConcurrentHashMap<>());

        if (meal.isNew()) {
            meal.setId(counter.incrementAndGet());
            mealsMap.put(meal.getId(), meal);
            return meal;
        }
        // handle case: update, but not present in storage
        return mealsMap.computeIfPresent(meal.getId(), (id, oldMeal) -> meal);
    }

    @Override
    public boolean delete(int id, int userId) {
        log.info("delete meal {} for user {}", id, userId);
        Map<Integer, Meal> mealsMap = repository.get(userId);
        return (mealsMap != null && mealsMap.remove(id) != null);
    }

    @Override
    public Meal get(int id, int userId) {
        log.info("get meal {} for user {}", id, userId);
        Map<Integer, Meal> mealsMap = repository.get(userId);
        return mealsMap != null ? mealsMap.get(id) : null;
    }

    @Override
    public List<Meal> getAll(int userId) {
        log.info("get all meals for user {}", userId);
        return getAllByFilter(userId, meal -> true);
    }

    @Override
    public List<Meal> getAllFilteredByDate(int userId, LocalDate startDate, LocalDate endDate) {
        log.info("get meals between {} and {} for user {}", startDate, endDate, userId);
        return getAllByFilter(userId, meal -> DateTimeUtil.isBetweenInclusive(meal.getDate(), startDate, endDate));
    }

    private List<Meal> getAllByFilter(int userId, Predicate<Meal> filter) {
        Map<Integer, Meal> mealsMap = repository.get(userId);
        return mealsMap != null ? mealsMap.values()
                .stream()
                .filter(filter)
                .sorted(Comparator.comparing(Meal::getDateTime).reversed())
                .collect(Collectors.toList())
                : Collections.emptyList();
    }
}

