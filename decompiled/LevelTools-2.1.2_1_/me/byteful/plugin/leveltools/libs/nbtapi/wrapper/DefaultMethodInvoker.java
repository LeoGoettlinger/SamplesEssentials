package me.byteful.plugin.leveltools.libs.nbtapi.wrapper;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import me.byteful.plugin.leveltools.libs.nbtapi.NbtApiException;

class DefaultMethodInvoker {
   private static Method invokeDefaultMethod;

   public static Object invokeDefault(Class<?> srcInt, Object target, Method method, Object[] args) {
      if (invokeDefaultMethod != null) {
         try {
            return invokeDefaultMethod.invoke(null, target, method, args);
         } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException var5) {
            throw new NbtApiException("Error while trying to invoke a default method for Java 9+. " + target + " " + method + " " + Arrays.toString(args), var5);
         }
      } else {
         try {
            Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
            constructor.setAccessible(true);
            return constructor.newInstance(srcInt).in(srcInt).unreflectSpecial(method, srcInt).bindTo(target).invokeWithArguments(args);
         } catch (Throwable var6) {
            throw new NbtApiException("Error while trying to invoke a default method for Java 8. " + target + " " + method + " " + Arrays.toString(args), var6);
         }
      }
   }

   static {
      try {
         invokeDefaultMethod = InvocationHandler.class.getDeclaredMethod("invokeDefault", Object.class, Method.class, Object[].class);
         invokeDefaultMethod.setAccessible(true);
      } catch (SecurityException | NoSuchMethodException var1) {
      }
   }
}
