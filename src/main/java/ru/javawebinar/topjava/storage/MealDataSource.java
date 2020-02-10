package ru.javawebinar.topjava.storage;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.MealsUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MealDataSource implements DataSource<Meal> {
    public static final DataSource<Meal> INSTANCE = new MealDataSource();
    private Map<Integer, Meal> repository = Collections.synchronizedMap(new HashMap<>());
    private AtomicInteger counter = new AtomicInteger(0);

    {
        List<Meal> meals = MealsUtil.initializeMeals();
        meals.forEach(this::addOrUpdate);
    }

    private MealDataSource() {

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
        synchronized (meal) {
            if (meal.isNew()) {
                meal.setId(counter.incrementAndGet());
            }
        }
        repository.merge(meal.getId(), meal, (oldMeal, newMeal) -> newMeal);
    }

    @Override
    public boolean deleteById(int id) {
        return repository.remove(id) != null;
    }

    @Override
    public Meal getById(int id) {
        return repository.get(id);
    }

    @Override
    public Collection<Meal> getAll() {
        return repository.values();
    }
}
