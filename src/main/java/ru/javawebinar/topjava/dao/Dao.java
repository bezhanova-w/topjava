package ru.javawebinar.topjava.dao;

import java.util.Collection;

public interface Dao<T> {
   void add(T t);
   void update(T t);
   boolean delete(int id);
   T getById(int id);
   Collection<T> getAll();

   default boolean isValid(T t) {
      return true;
   }

   default String getVerificationCriteria() {
      return "";
   }
}
