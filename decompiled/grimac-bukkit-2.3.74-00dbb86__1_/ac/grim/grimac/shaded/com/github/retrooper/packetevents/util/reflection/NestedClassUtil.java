package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.reflection;

import java.lang.annotation.Annotation;

public class NestedClassUtil {
   public static Class<?> getNestedClass(Class<?> cls, String name) {
      if (cls == null) {
         return null;
      } else {
         for (Class<?> subClass : cls.getDeclaredClasses()) {
            if (subClass.getSimpleName().equals(name)) {
               return subClass;
            }
         }

         return null;
      }
   }

   public static Class<?> getNestedClass(Class<?> cls, int index) {
      if (cls == null) {
         return null;
      } else {
         int currentIndex = 0;

         for (Class<?> subClass : cls.getDeclaredClasses()) {
            if (index == currentIndex++) {
               return subClass;
            }
         }

         return null;
      }
   }

   public static Class<?> getNestedClass(Class<?> cls, Annotation annotation, int index) {
      int currentIndex = 0;

      for (Class<?> subClass : cls.getDeclaredClasses()) {
         if (subClass.isAnnotationPresent((Class<? extends Annotation>)annotation.getClass()) && index == currentIndex++) {
            return subClass;
         }
      }

      return null;
   }
}
