package revxrsal.commands.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Preconditions {
   private Preconditions() {
   }

   public static <T> void notEmpty(T[] array, String err) {
      if (array.length == 0) {
         throw new IllegalStateException(err);
      }
   }

   public static <T> void notEmpty(Collection<T> collection, String err) {
      if (collection.size() == 0) {
         throw new IllegalStateException(err);
      }
   }

   public static <T> void notEmpty(String s, String err) {
      if (s.length() == 0) {
         throw new IllegalStateException(err);
      }
   }

   public static void checkArgument(boolean expr, String err) {
      if (!expr) {
         throw new IllegalArgumentException(err);
      }
   }

   public static int coerceIn(int value, int min, int max) {
      return value < min ? min : Math.min(value, max);
   }

   public static int coerceAtMost(int value, int max) {
      return Math.min(value, max);
   }

   public static int coerceAtLeast(int value, int min) {
      return Math.max(value, min);
   }

   public static <T> T notNull(T t, String err) {
      return Objects.requireNonNull(t, err + " cannot be null!");
   }

   public static void checkCallableStatic(@Nullable Object instance, @NotNull Method method) {
      if (instance == null && !Modifier.isStatic(method.getModifiers())) {
         throw new IllegalArgumentException(
            "The given method is not static, and no instance was provided. Either mark the function as static with @JvmStatic, or pass the object/companion object value for the instance."
         );
      }
   }
}
