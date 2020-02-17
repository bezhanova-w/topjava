package ru.javawebinar.topjava.repository.inmemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class InMemoryUserRepository implements UserRepository {
    public static final int USER_ID_1 = 1;
    public static final int USER_ID_2 = 2;

    private static final Logger log = LoggerFactory.getLogger(InMemoryUserRepository.class);

    private final Map<Integer, User> repository = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public boolean delete(int id) {
        log.info("delete user {}", id);
        return repository.remove(id) != null;
    }

    @Override
    public User save(User user) {
        log.info("save user {}", user);
        if (user.isNew()) {
            user.setId(counter.incrementAndGet());
            repository.put(user.getId(), user);
            return user;
        }
        // handle case: update, but not present in storage
        return repository.computeIfPresent(user.getId(), (id, oldUser) -> user);
    }

    @Override
    public User get(int id) {
        log.info("get user {}", id);
        return repository.get(id);
    }

    @Override
    public List<User> getAll() {
        log.info("get all users");
        return repository.values()
                .stream()
                .sorted(Comparator.comparing(User::getName).thenComparing(User::getEmail))
                .collect(Collectors.toList());
    }

    @Override
    public User getByEmail(String email) {
        log.info("get user by email {}", email);
        return repository.values()
                .stream()
                .filter(user -> Objects.equals(user.getEmail(), email))
                .findAny().orElse(null);
    }
}
