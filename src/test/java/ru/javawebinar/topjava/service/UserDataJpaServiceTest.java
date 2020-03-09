package ru.javawebinar.topjava.service;

import org.junit.Assert;
import org.springframework.test.context.ActiveProfiles;
import ru.javawebinar.topjava.Profiles;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.util.exception.NotFoundException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import static ru.javawebinar.topjava.MealTestData.MEALS;
import static ru.javawebinar.topjava.MealTestData.MEAL_MATCHER;
import static ru.javawebinar.topjava.UserTestData.*;

@ActiveProfiles(Profiles.DATAJPA)
public class UserDataJpaServiceTest extends AbstractUserServiceTest{
    static {
        className = MethodHandles.lookup().lookupClass().getSimpleName();
    }

    @Override
    public void getWithMeals() throws Exception {
        User user = service.getWithMeals(USER_ID);
        USER_MATCHER.assertMatch(user, USER);
        MEAL_MATCHER.assertMatch(user.getMeals(), MEALS);
    }

    @Override
    public void getNotFoundWithMeals() throws Exception {
        Assert.assertThrows(NotFoundException.class,
                () -> service.getWithMeals(1));
    }

    @Override
    public void getWithNoMeals() throws Exception {
        User user = service.getWithMeals(USER_WITHOUT_MEALS_ID);
        USER_MATCHER.assertMatch(user, USER_WITHOUT_MEALS);
        MEAL_MATCHER.assertMatch(user.getMeals(), Collections.emptyList());
    }
}
