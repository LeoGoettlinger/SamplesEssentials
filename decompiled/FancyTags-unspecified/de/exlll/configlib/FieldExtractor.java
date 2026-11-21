package de.exlll.configlib;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.stream.Stream;

@FunctionalInterface
interface FieldExtractor extends Function<Class<?>, Stream<Field>> {
   Stream<Field> extract(Class<?> var1);

   default Stream<Field> apply(Class<?> cls) {
      return this.extract(cls);
   }
}
