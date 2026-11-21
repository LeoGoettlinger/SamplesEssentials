package de.exlll.configlib;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

enum FieldFilters implements FieldFilter {
   DEFAULT {
      public boolean test(Field field) {
         Validator.requireNonNull(field, "field");
         if (field.isSynthetic()) {
            return false;
         } else if (Reflect.isIgnored(field)) {
            return false;
         } else {
            int modifiers = field.getModifiers();
            return !Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers);
         }
      }
   };
}
