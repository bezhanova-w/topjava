package ru.javawebinar.topjava.service;

import org.junit.Assert;
import org.springframework.test.context.ActiveProfiles;
import ru.javawebinar.topjava.Profiles;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.exception.NotFoundException;
import java.lang.invoke.MethodHandles;
import static ru.javawebinar.topjava.MealTestData.*;
import static ru.javawebinar.topjava.UserTestData.*;

@ActiveProfiles(Profiles.DATAJPA)
public class MealDataJpaServiceTest extends AbstractMealServiceTest{

    static {
        //https://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java
        className = MethodHandles.lookup().lookupClass().getSimpleName();
    }

    @Override
    public void getWithUser() throws Exception {
        Meal actual = service.getWithUser(ADMIN_MEAL_ID, ADMIN_ID);
        MEAL_MATCHER.assertMatch(actual, ADMIN_MEAL1);
        USER_MATCHER.assertMatch(actual.getUser(), ADMIN);
    }

    @Override
    public void getNotFoundWithUser() throws Exception {
        Assert.assertThrows(NotFoundException.class,
                () -> service.getWithUser(MEAL1_ID, ADMIN_ID));    }

    @Override
    public void getNotOwnWithUser() throws Exception {
        Assert.assertThrows(NotFoundException.class,
                () -> service.getWithUser(MEAL1_ID, ADMIN_ID));    }
}