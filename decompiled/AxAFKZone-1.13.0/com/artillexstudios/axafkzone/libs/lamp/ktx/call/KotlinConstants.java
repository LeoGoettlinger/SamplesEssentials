package com.artillexstudios.axafkzone.libs.lamp.ktx.call;

import com.artillexstudios.axafkzone.libs.lamp.util.Suppliers;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KotlinConstants {
   public static final Object ABSENT_VALUE = new Object();
   private static final Supplier<Class<?>> CONTINUATION = Suppliers.lazy(() -> findClass("kotlin.coroutines.Continuation"));
   private static final Supplier<Class<? extends Annotation>> METADATA = Suppliers.lazy(() -> {
      Class<?> metadata = findClass("kotlin.Metadata");
      return metadata == null ? null : metadata.asSubclass(Annotation.class);
   });

   private KotlinConstants() {
   }

   @Nullable
   private static Class<?> findClass(@NotNull String name) {
      try {
         return Class.forName(name);
      } catch (ClassNotFoundException var2) {
         return null;
      }
   }

   public static boolean isStaticFinal(int modifiers) {
      return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
   }

   @Nullable
   public static Object defaultPrimitiveValue(Class<?> type) {
      if (type == int.class) {
         return 0;
      } else if (type == long.class) {
         return 0L;
      } else if (type == float.class) {
         return 0.0F;
      } else if (type == double.class) {
         return 0.0;
      } else if (type == short.class) {
         return (short)0;
      } else if (type == byte.class) {
         return (byte)0;
      } else if (type == boolean.class) {
         return false;
      } else {
         return type == char.class ? '\u0000' : null;
      }
   }

   @Nullable
   public static Class<?> continuation() {
      return CONTINUATION.get();
   }

   public static boolean isKotlinClass(@NotNull Class<?> cl) {
      return METADATA.get() != null && cl.isAnnotationPresent(METADATA.get());
   }
}
