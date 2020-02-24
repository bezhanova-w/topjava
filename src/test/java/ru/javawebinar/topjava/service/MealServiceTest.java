package ru.javawebinar.topjava.service;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import ru.javawebinar.topjava.UserTestData;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static ru.javawebinar.topjava.MealTestData.*;

@ContextConfiguration({
        "classpath:spring/spring-app.xml",
        "classpath:spring/spring-db.xml"
})
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:db/populateDB.sql", config = @SqlConfig(encoding = "UTF-8"))
public class MealServiceTest {

    static {
        // Only for postgres driver logging
        // It uses java.util.logging and logged via jul-to-slf4j bridge
        SLF4JBridgeHandler.install();
    }

    @AfterClass
    public static void doAfterClass() {
        SLF4JBridgeHandler.uninstall();
    }

    @Autowired
    private MealService service;

    @Test
    public void get() {
        Meal meal = service.get(START_USER_MEAL_ID, UserTestData.USER_ID);
        assertMatch(meal, USER_MEAL_0);
    }

    @Test(expected = NotFoundException.class)
    public void getNoSuchMealId() throws Exception {
        service.get(1, UserTestData.USER_ID);
    }

    @Test(expected = NotFoundException.class)
    public void getNoSuchMealIdForUser() throws Exception {
        service.get(START_USER_MEAL_ID, UserTestData.ADMIN_ID);
    }

    @Test(expected = NotFoundException.class)
    public void delete() throws Exception {
        service.delete(START_USER_MEAL_ID, UserTestData.USER_ID);
        service.get(START_USER_MEAL_ID, UserTestData.USER_ID);
    }

    @Test(expected = NotFoundException.class)
    public void deleteNoSuchMealId() throws Exception {
        service.delete(1, UserTestData.USER_ID);
    }

    @Test(expected = NotFoundException.class)
    public void deleteNoSuchMealIdForUser() throws Exception {
        service.delete(START_USER_MEAL_ID, UserTestData.ADMIN_ID);
    }

    @Test
    public void getBetweenHalfOpen() {
        List<Meal> meals = service.getBetweenHalfOpen(LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 1, 30),
                UserTestData.USER_ID);
        List<Meal> mealsExpected = Arrays.asList(USER_MEAL_2, USER_MEAL_1, USER_MEAL_0);
        assertMatch(meals, mealsExpected);
    }

    @Test
    public void getAll() {
        List<Meal> all = service.getAll(UserTestData.USER_ID);
        List<Meal> allExpected = Arrays.asList(USER_MEAL_6,
                USER_MEAL_5, USER_MEAL_4, USER_MEAL_3,
                USER_MEAL_2, USER_MEAL_1, USER_MEAL_0);
        assertMatch(all, allExpected);
    }

    @Test
    public void update() {
        Meal updated = getUpdated();
        service.update(updated, UserTestData.USER_ID);
        assertMatch(service.get(START_USER_MEAL_ID, UserTestData.USER_ID), updated);
    }

    @Test(expected = NotFoundException.class)
    public void updateNoSuchMealId() throws Exception {
        Meal updated = getUpdated();
        updated.setId(1);
        service.update(updated, UserTestData.USER_ID);
    }

    @Test(expected = NotFoundException.class)
    public void updateNoSuchMealIdForUser() throws Exception {
        Meal updated = getUpdated();
        service.update(updated, UserTestData.ADMIN_ID);
    }

    @Test
    public void create() {
        Meal newMeal = getNew();
        Meal created = service.create(newMeal, UserTestData.USER_ID);
        Integer newId = created.getId();
        newMeal.setId(newId);
        assertMatch(created, newMeal);
        assertMatch(service.get(newId, UserTestData.USER_ID), newMeal);
    }

    @Test(expected = DataAccessException.class)
    public void duplicateDateTimeCreate() throws Exception {
        Meal newMeal = getNew();
        newMeal.setDateTime(USER_MEAL_0.getDateTime());
        service.create(newMeal, UserTestData.USER_ID);
    }
}