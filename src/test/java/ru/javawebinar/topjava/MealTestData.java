package ru.javawebinar.topjava;

import ru.javawebinar.topjava.model.Meal;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.javawebinar.topjava.model.AbstractBaseEntity.START_SEQ;

public class MealTestData {
    public static final int START_USER_MEAL_ID = START_SEQ + 2;
    public static final int START_ADMIN_MEAL_ID = START_SEQ + 9;

    public static final Meal USER_MEAL_0 = new Meal(START_USER_MEAL_ID, LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500);
    public static final Meal USER_MEAL_1 = new Meal(START_USER_MEAL_ID + 1, LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000);
    public static final Meal USER_MEAL_2 = new Meal(START_USER_MEAL_ID + 2, LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500);
    public static final Meal USER_MEAL_3 = new Meal(START_USER_MEAL_ID + 3, LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100);
    public static final Meal USER_MEAL_4 = new Meal(START_USER_MEAL_ID + 4, LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000);
    public static final Meal USER_MEAL_5 = new Meal(START_USER_MEAL_ID + 5, LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500);
    public static final Meal USER_MEAL_6 = new Meal(START_USER_MEAL_ID + 6, LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410);

    public static final Meal ADMIN_MEAL_0 = new Meal(START_ADMIN_MEAL_ID, LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Админ завтрак", 510);
    public static final Meal ADMIN_MEAL_1 = new Meal(START_ADMIN_MEAL_ID + 1, LocalDateTime.of(2020, Month.JANUARY, 30, 21, 0), "Админ ужин", 1500);

    public static Meal getNew() {
        return new Meal(null, LocalDateTime.of(2020, Month.FEBRUARY, 23, 18, 0), "New dinner", 1111);
    }

    public static Meal getUpdated() {
        Meal updated = new Meal(USER_MEAL_0);
        updated.setDescription("Updated breakfast");
        updated.setCalories(300);
        return updated;
    }

    public static void assertMatch(Meal actual, Meal expected) {
        assertThat(actual).isEqualToComparingFieldByField(expected);
    }

    public static void assertMatch(Iterable<Meal> actual, Meal... expected) {
        assertMatch(actual, Arrays.asList(expected));
    }

    public static void assertMatch(Iterable<Meal> actual, Iterable<Meal> expected) {
        assertThat(actual).usingFieldByFieldElementComparator().isEqualTo(expected);
    }
}
