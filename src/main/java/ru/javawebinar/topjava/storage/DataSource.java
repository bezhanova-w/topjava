package ru.javawebinar.topjava.storage;

import java.util.Collection;

public interface DataSource<T> {

    void add(T t);
    void update(T t);
    boolean deleteById(int id);
    T getById(int id);
    Collection<T> getAll();
}
