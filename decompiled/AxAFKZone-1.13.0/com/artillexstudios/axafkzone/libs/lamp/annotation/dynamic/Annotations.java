package com.artillexstudios.axafkzone.libs.lamp.annotation.dynamic;

import com.artillexstudios.axafkzone.libs.lamp.util.Preconditions;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public final class Annotations {
   @NotNull
   public static <T extends Annotation> T create(@NotNull Class<T> type) {
      return create(type, Collections.emptyMap());
   }

   @NotNull
   public static <T extends Annotation> T create(@NotNull Class<T> type, @NotNull Map<String, Object> members) {
      Preconditions.notNull(type, "type");
      Preconditions.notNull(members, "members");
      return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new Annotations.DynamicAnnotationHandler(type, members)));
   }

   @NotNull
   public static <T extends Annotation> T create(@NotNull Class<T> type, @NotNull Object... members) {
      Preconditions.notNull(type, "type");
      Preconditions.notNull(members, "members");
      if (members.length % 2 != 0) {
         throw new IllegalArgumentException("Cannot have a non-even amount of members! Found " + members.length);
      } else {
         Map<String, Object> values = new HashMap<>();

         for (int i = 0; i < members.length; i += 2) {
            String key = String.valueOf(members[i]);
            Object value = members[i + 1];
            values.put(key, value);
         }

         return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new Annotations.DynamicAnnotationHandler(type, values)));
      }
   }

   private static int hashCode(Class<? extends Annotation> type, Map<String, Object> members) {
      int result = 0;

      for (Method method : type.getDeclaredMethods()) {
         String name = method.getName();
         Object value = members.get(name);
         result += 127 * name.hashCode() ^ Arrays.deepHashCode(new Object[]{value}) - 31;
      }

      return result;
   }

   private static boolean equals(Class<? extends Annotation> type, Map<String, Object> members, Object other) throws Exception {
      if (!type.isInstance(other)) {
         return false;
      } else {
         for (Method method : type.getDeclaredMethods()) {
            String name = method.getName();
            if (!Arrays.deepEquals(new Object[]{method.invoke(other)}, new Object[]{members.get(name)})) {
               return false;
            }
         }

         return true;
      }
   }

   private static String toString(Class<? extends Annotation> type, Map<String, Object> members) {
      StringBuilder sb = new StringBuilder().append("@").append(type.getName()).append("(");
      StringJoiner joiner = new StringJoiner(", ");

      for (Entry<String, Object> entry : members.entrySet()) {
         joiner.add(entry.getKey() + "=" + deepToString(entry.getValue()));
      }

      sb.append(joiner);
      return sb.append(")").toString();
   }

   private static String deepToString(Object arg) {
      String s = Arrays.deepToString(new Object[]{arg});
      return s.substring(1, s.length() - 1);
   }

   private static class DynamicAnnotationHandler implements InvocationHandler {
      private final Class<? extends Annotation> annotationType;
      private final Map<String, Object> annotationMembers;

      DynamicAnnotationHandler(Class<? extends Annotation> annotationType, Map<String, Object> annotationMembers) {
         this.annotationType = annotationType;
         this.annotationMembers = new HashMap<>(annotationMembers);

         for (Method method : annotationType.getDeclaredMethods()) {
            this.annotationMembers.putIfAbsent(method.getName(), method.getDefaultValue());
         }
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         String var4 = method.getName();
         switch (var4) {
            case "toString":
               return Annotations.toString(this.annotationType, this.annotationMembers);
            case "hashCode":
               return Annotations.hashCode(this.annotationType, this.annotationMembers);
            case "equals":
               return Annotations.equals(this.annotationType, this.annotationMembers, args[0]);
            case "annotationType":
               return this.annotationType;
            default:
               Object v = this.annotationMembers.get(method.getName());
               if (v == null) {
                  throw new AbstractMethodError(method.getName());
               } else {
                  return v instanceof Supplier ? ((Supplier)v).get() : v;
               }
         }
      }
   }
}
