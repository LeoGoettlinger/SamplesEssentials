package me.chancesd.pvpmanager.sdutils.utils;

import java.lang.reflect.InvocationTargetException;

public class ReflectionUtil {
   public static Object invokeMethods(Object object, String... methods) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
      Object result = object;

      for (String method : methods) {
         result = result.getClass().getDeclaredMethod(method).invoke(result);
      }

      return result;
   }
}
