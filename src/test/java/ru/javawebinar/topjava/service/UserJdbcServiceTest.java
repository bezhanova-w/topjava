package ru.javawebinar.topjava.service;

import org.springframework.test.context.ActiveProfiles;
import ru.javawebinar.topjava.Profiles;

import java.lang.invoke.MethodHandles;

@ActiveProfiles(Profiles.JDBC)
public class UserJdbcServiceTest extends AbstractUserServiceTest{
    static {
        className = MethodHandles.lookup().lookupClass().getSimpleName();
    }
}
