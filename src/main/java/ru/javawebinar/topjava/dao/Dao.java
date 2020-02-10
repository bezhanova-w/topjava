package ru.javawebinar.topjava.dao;

import java.util.List;

public interface Dao<T> {
   void add(T t);
   void update(T t);
   boolean delete(int id);
   T getById(int id);
   List<T> getAll();
}
