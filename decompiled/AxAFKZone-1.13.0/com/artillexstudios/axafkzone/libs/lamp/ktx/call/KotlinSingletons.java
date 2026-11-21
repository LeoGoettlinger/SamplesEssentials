package com.artillexstudios.axafkzone.libs.lamp.ktx.call;

import com.artillexstudios.axafkzone.libs.lamp.core.reflect.MethodCaller;
import com.artillexstudios.axafkzone.libs.lamp.core.reflect.MethodCallerFactory;
import com.artillexstudios.axafkzone.libs.lamp.util.Collections;
import com.artillexstudios.axafkzone.libs.lamp.util.Preconditions;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KotlinSingletons {
   private static final String COMPANION_NAME = "Companion";

   private KotlinSingletons() {
   }

   public static MethodCaller getCallerForNonDefault(@NotNull Method method) {
      Class<?>[] parameterTypes = method.getParameterTypes();
      MethodCaller caller = wrapMethod(method);
      boolean isStatic = Modifier.isStatic(method.getModifiers());
      if (isStatic) {
         return caller;
      } else {
         return parameterTypes.length > 0 && parameterTypes[0] == method.getDeclaringClass() ? (instance, arguments) -> {
            Object[] boundArgs = Collections.insertAtBeginning(arguments, instance);
            return caller.call(instance, boundArgs);
         } : caller;
      }
   }

   @Nullable
   public static Object findCompanion(Class<?> type) {
      Map<String, Field> fields = Arrays.stream(type.getDeclaredFields())
         .filter(field -> Modifier.isPublic(field.getModifiers()) && KotlinConstants.isStaticFinal(field.getModifiers()))
         .collect(Collectors.toMap(Field::getName, f -> (Field)f));

      try {
         Class<?> companion = Class.forName(type.getName() + "$" + "Companion");
         Field companionField = fields.get(companion.getSimpleName());
         if (companionField != null) {
            return fetch(companionField);
         }
      } catch (ClassNotFoundException var9) {
      }

      for (Class<?> declaredClass : type.getDeclaredClasses()) {
         String name = declaredClass.getSimpleName();
         Field companionField = fields.get(name);
         if (companionField != null) {
            Object singleton = fetch(companionField);
            if (singleton != null) {
               return singleton;
            }
         }
      }

      return null;
   }

   private static void makeAccessible(@NotNull AccessibleObject accessibleObject) {
      if (!accessibleObject.isAccessible()) {
         accessibleObject.setAccessible(true);
      }
   }

   private static Object fetch(@NotNull Field field) {
      try {
         makeAccessible(field);
         return field.get(null);
      } catch (IllegalAccessException var2) {
         throw new IllegalStateException("Failed to access the field", var2);
      }
   }

   static MethodCaller wrapMethod(@NotNull Method method) {
      try {
         Preconditions.notNull(method, "method");
         return MethodCallerFactory.methodHandles().createFor(method);
      } catch (Throwable var2) {
         throw var2;
      }
   }
}
