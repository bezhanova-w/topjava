package ru.javawebinar.topjava.service;

import org.springframework.test.context.ActiveProfiles;
import ru.javawebinar.topjava.Profiles;

import java.lang.invoke.MethodHandles;

@ActiveProfiles(Profiles.JPA)
public class MealJpaServiceTest extends AbstractMealServiceTest{
    static {
        className = MethodHandles.lookup().lookupClass().getSimpleName();
    }
}