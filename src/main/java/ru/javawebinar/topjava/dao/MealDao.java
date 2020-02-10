package ru.javawebinar.topjava.dao;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.storage.*;

import java.util.Collection;

public class MealDao implements Dao<Meal> {
    private DataSource<Meal> dataSource;

    public MealDao() {
        dataSource = MealDataSource.INSTANCE;
    }

    @Override
    public void add(Meal meal) {
        dataSource.add(meal);
    }

    @Override
    public void update(Meal meal) {
        dataSource.update(meal);
    }

    @Override
    public boolean isValid(Meal meal) {
        String description = meal.getDescription();
        return (description != null && description.trim().length() >= 2 && description.trim().length() <= 120)
                && (meal.getDateTime() != null)
                && (meal.getCalories() >= 10 && meal.getCalories() <= 5000);
    }

    @Override
    public String getVerificationCriteria() {
        return "Размер [Описание] должен быть между 2 и 120.\n"
                + "[Описание] не должно быть пустым.\n"
                + "Значение [Калории] должно быть между 10 и 5000.\n"
                + "Значение [Дата/Время] не должно быть пустым.";
    }

    @Override
    public boolean delete(int id) {
        return dataSource.deleteById(id);
    }

    @Override
    public Meal getById(int id) {
        return dataSource.getById(id);
    }

    @Override
    public Collection<Meal> getAll() {
        return dataSource.getAll();
    }
}
