package de.exlll.configlib;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

enum FieldExtractors implements FieldExtractor {
   CONFIGURATION {
      @Override
      public Stream<Field> extract(Class<?> cls) {
         Validator.requireNonNull(cls, "configuration class");
         Validator.requireConfigurationClass(cls);
         List<Class<?>> classes = FieldExtractors.extractClassesWhile(cls, Reflect::isConfigurationClass);
         List<Field> fields = classes.stream().flatMap(c -> Arrays.stream(c.getDeclaredFields())).filter(FieldFilters.DEFAULT).toList();
         FieldExtractors.requireNoShadowing(fields);
         return fields.stream();
      }
   };

   private static void requireNoShadowing(List<Field> fields) {
      Map<String, Class<?>> map = new LinkedHashMap<>();

      for (Field field : fields) {
         String fieldName = field.getName();
         Class<?> fieldClass = field.getDeclaringClass();
         if (map.containsKey(fieldName)) {
            Class<?> superClass = map.get(fieldName);
            String msg = "Shadowing of fields is not supported. Field '"
               + fieldName
               + "' of class "
               + fieldClass.getSimpleName()
               + " shadows field '"
               + fieldName
               + "' of class "
               + superClass.getSimpleName()
               + ".";
            throw new ConfigurationException(msg);
         }

         map.put(fieldName, fieldClass);
      }
   }

   private static List<Class<?>> extractClassesWhile(Class<?> cls, Predicate<Class<?>> condition) {
      List<Class<?>> classes = new ArrayList<>();

      for (Class<?> current = cls; condition.test(current); current = current.getSuperclass()) {
         classes.add(current);
      }

      Collections.reverse(classes);
      return classes;
   }
}
