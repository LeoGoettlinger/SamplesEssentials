package org.skriptlang.skript.util;

import java.lang.reflect.Modifier;

public final class ClassUtils {
   public static boolean isNormalClass(Class<?> clazz) {
      return !clazz.isAnnotation() && !clazz.isArray() && !clazz.isPrimitive() && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
   }

   public static int hierarchyDistance(Class<?> a, Class<?> b) {
      if (!a.isAssignableFrom(b)) {
         return -1;
      } else if (a.equals(b)) {
         return 0;
      } else {
         int distance = 0;

         for (Class<?> current = b; current != null && !a.equals(current); distance++) {
            current = current.getSuperclass();
         }

         return distance;
      }
   }

   public static int hierarchyDistanceBetween(Class<?> a, Class<?> b) {
      int dist = hierarchyDistance(a, b);
      return dist != -1 ? dist : hierarchyDistance(b, a);
   }

   public static boolean isRelatedTo(Class<?> a, Class<?> b) {
      return a.isAssignableFrom(b) || b.isAssignableFrom(a);
   }
}
