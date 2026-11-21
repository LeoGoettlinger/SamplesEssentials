package org.enginehub.piston.inject;

import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

class Annotations {
   private static final MapJoiner JOINER = Joiner.on(", ").withKeyValueSeparator("=");
   private static final Map<Annotations.MethodKey, Annotations.AnnoMethod> ANNOTATION_METHODS = ImmutableMap.of(
      Annotations.MethodKey.of(Class.class, "annotationType"),
      (Annotations.AnnoMethod)(type, members, args) -> type,
      Annotations.MethodKey.of(boolean.class, "equals", Object.class),
      (Annotations.AnnoMethod)(type, members, args) -> {
         if (!type.isInstance(args[0])) {
            return false;
         } else {
            for (Method method : type.getDeclaredMethods()) {
               String name = method.getName();
               if (!Objects.equals(method.invoke(args[0]), members.get(name))) {
                  return false;
               }
            }

            return true;
         }
      },
      Annotations.MethodKey.of(int.class, "hashCode"),
      (Annotations.AnnoMethod)(type, members, args) -> {
         int result = 0;

         for (String name : members.keySet()) {
            Object value = members.get(name);
            result += 127 * name.hashCode() ^ Objects.hash(value) - 31;
         }

         return result;
      },
      Annotations.MethodKey.of(String.class, "toString"),
      (Annotations.AnnoMethod)(type, members, args) -> {
         StringBuilder output = new StringBuilder("@").append(type.getName()).append('(');
         JOINER.appendTo(output, members.entrySet().stream().map(e -> Maps.immutableEntry(e.getKey(), valueToString(e.getValue()))).iterator());
         return output.append(')').toString();
      }
   );
   private static final Lookup LOOKUP = MethodHandles.lookup();

   private static String valueToString(@Nullable Object value) {
      if (value == null) {
         return "null";
      } else {
         return value.getClass().isArray() ? Arrays.deepToString((Object[])value) : value.toString();
      }
   }

   static Annotation allDefaultsAnnotation(Class<? extends Annotation> annotationType) {
      Map<String, Object> members = Stream.of(annotationType.getDeclaredMethods()).collect(Collectors.toMap(Method::getName, Method::getDefaultValue));
      return (Annotation)Proxy.newProxyInstance(annotationType.getClassLoader(), new Class[]{annotationType}, (proxy, method, args) -> {
         Annotations.AnnoMethod call = ANNOTATION_METHODS.get(Annotations.MethodKey.from(method));
         if (call != null) {
            return call.invoke(annotationType, members, args);
         } else {
            Object value = members.get(method.getName());
            if (value != null) {
               return value;
            } else {
               throw new IllegalStateException("Unknown method on " + annotationType + ": " + method);
            }
         }
      });
   }

   @FunctionalInterface
   private interface AnnoMethod {
      Object invoke(Class<? extends Annotation> var1, Map<String, Object> var2, Object[] var3) throws Exception;
   }

   private static final class MethodKey {
      private final String name;
      private final ImmutableList<Class<?>> signature;

      static Annotations.MethodKey from(Method method) {
         return of(method.getReturnType(), method.getName(), method.getParameterTypes());
      }

      static Annotations.MethodKey of(Class<?> rtype, String name, Class<?>... ptypes) {
         return new Annotations.MethodKey(name, ImmutableList.builder().add(rtype).add(ptypes).build());
      }

      private MethodKey(String name, List<Class<?>> signature) {
         this.name = name;
         this.signature = ImmutableList.copyOf(signature);
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Annotations.MethodKey methodKey = (Annotations.MethodKey)o;
            return this.name.equals(methodKey.name) && this.signature.equals(methodKey.signature);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.name, this.signature);
      }
   }
}
