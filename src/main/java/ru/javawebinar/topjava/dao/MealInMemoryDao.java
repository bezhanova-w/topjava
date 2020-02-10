package ru.javawebinar.topjava.dao;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.MealsUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MealInMemoryDao implements Dao<Meal> {
    private Map<Integer, Meal> repository = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger(0);

    {
        List<Meal> meals = MealsUtil.initializeMeals();
        meals.forEach(this::addOrUpdate);
    }

    @Override
    public void add(Meal meal) {
        addOrUpdate(meal);
    }

    @Override
    public void update(Meal meal) {
        addOrUpdate(meal);
    }

    public void addOrUpdate(Meal meal) {
        if (meal.isNew()) {
            meal.setId(counter.incrementAndGet());
        }
        repository.merge(meal.getId(), meal, (oldMeal, newMeal) -> newMeal);
    }

    @Override
    public boolean delete(int id) {
        return repository.remove(id) != null;
    }

    @Override
    public Meal getById(int id) {
        return repository.get(id);
    }

    @Override
    public List<Meal> getAll() {
        return new ArrayList<>(repository.values());
    }
}
