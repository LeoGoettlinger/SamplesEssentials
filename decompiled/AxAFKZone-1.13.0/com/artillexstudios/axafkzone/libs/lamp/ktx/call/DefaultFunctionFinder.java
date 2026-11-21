package com.artillexstudios.axafkzone.libs.lamp.ktx.call;

import com.artillexstudios.axafkzone.libs.lamp.core.reflect.MethodCaller;
import com.artillexstudios.axafkzone.libs.lamp.util.Collections;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DefaultFunctionFinder {
   private DefaultFunctionFinder() {
   }

   @Nullable
   static CallableMethod findDefaultFunction(@NotNull Method method) {
      try {
         if (method.getParameterCount() == 0) {
            throw new IllegalArgumentException("Method has no parameters!");
         } else {
            List<Class<?>> syntheticParams = getSyntheticParameters(method);
            String name = getDefaultMethodName(method);
            Method defaultMethod = getDeclaredMethodOrNull(name, method.getDeclaringClass(), syntheticParams);
            if (defaultMethod != null) {
               return CallableMethod.of(defaultMethod, KotlinSingletons.wrapMethod(defaultMethod));
            } else {
               syntheticParams.add(0, method.getDeclaringClass());
               defaultMethod = getDeclaredMethodOrNull(name, method.getDeclaringClass(), syntheticParams);
               if (defaultMethod != null) {
                  MethodCaller callerForDefault = bindInstanceParameter(defaultMethod);
                  return CallableMethod.of(defaultMethod, callerForDefault);
               } else {
                  Object companion = KotlinSingletons.findCompanion(method.getDeclaringClass());
                  if (companion == null) {
                     return null;
                  } else {
                     syntheticParams.set(0, companion.getClass());
                     defaultMethod = getDeclaredMethodOrNull(name, companion.getClass(), syntheticParams);
                     if (defaultMethod == null) {
                        return null;
                     } else {
                        MethodCaller callerForDefault = bindInstanceParameter(defaultMethod);
                        return CallableMethod.of(defaultMethod, callerForDefault);
                     }
                  }
               }
            }
         }
      } catch (Throwable var6) {
         throw var6;
      }
   }

   @NotNull
   private static MethodCaller bindInstanceParameter(Method method) {
      try {
         MethodCaller caller = KotlinSingletons.wrapMethod(method);
         return (instance, arguments) -> {
            Object[] boundArgs = Collections.insertAtBeginning(arguments, instance);
            return caller.call(instance, boundArgs);
         };
      } catch (Throwable var2) {
         throw var2;
      }
   }

   @Nullable
   private static Method getDeclaredMethodOrNull(@NotNull String name, @NotNull Class<?> type, @NotNull List<Class<?>> parameterTypes) {
      try {
         return type.getDeclaredMethod(name, parameterTypes.toArray(new Class[0]));
      } catch (NoSuchMethodException var4) {
         return null;
      }
   }

   @NotNull
   private static String getDefaultMethodName(@NotNull Method method) {
      return method.getName() + "$default";
   }

   @NotNull
   private static List<Class<?>> getSyntheticParameters(Method method) {
      List<Class<?>> parameters = new ArrayList<>(method.getParameterCount() + 3);
      java.util.Collections.addAll(parameters, method.getParameterTypes());
      parameters.add(int.class);
      parameters.add(Object.class);
      return parameters;
   }
}
