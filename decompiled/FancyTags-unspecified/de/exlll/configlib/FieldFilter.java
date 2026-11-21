package de.exlll.configlib;

import java.lang.reflect.Field;
import java.util.function.Predicate;

@FunctionalInterface
public interface FieldFilter extends Predicate<Field> {
   default FieldFilter and(Predicate<? super Field> other) {
      return field -> this.test(field) && other.test(field);
   }
}
