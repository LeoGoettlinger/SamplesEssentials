package org.jdbi.v3.core.internal;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jdbi.v3.core.internal.exceptions.Unchecked;

public class AnnotationFactory {
   private AnnotationFactory() {
   }

   public static <T extends Annotation> T create(Class<T> annotationType) {
      return create(annotationType, Collections.emptyMap());
   }

   public static <T extends Annotation> T create(Class<T> annotationType, Map<String, ?> values) {
      Arrays.stream(annotationType.getDeclaredMethods())
         .filter(m -> m.getDefaultValue() == null)
         .filter(m -> !values.containsKey(m.getName()))
         .findAny()
         .ifPresent(
            m -> {
               throw new IllegalArgumentException(
                  String.format(
                     "Cannot synthesize annotation @%s from %s.class because it has attribute '%s' without a default or specified value",
                     annotationType.getSimpleName(),
                     annotationType.getName(),
                     m.getName()
                  )
               );
            }
         );
      Class<?>[] interfaces = new Class[]{annotationType};
      InvocationHandler invocationHandler = getInvocationHandler(annotationType, values);
      return (T)Proxy.newProxyInstance(
         Optional.ofNullable(annotationType.getClassLoader()).orElseGet(Thread.currentThread()::getContextClassLoader), interfaces, invocationHandler
      );
   }

   private static <T extends Annotation> InvocationHandler getInvocationHandler(Class<T> annotationType, Map<String, ?> values) {
      Function<Method, Object> getValue = method -> Optional.ofNullable(values.get(method.getName())).orElseGet(method::getDefaultValue);
      int hashCode = Arrays.stream(annotationType.getDeclaredMethods()).mapToInt(m -> memberHash(m.getName(), getValue.apply(m))).sum();
      String toString = "@"
         + annotationType.getName()
         + "("
         + values.entrySet().stream().sorted(Comparator.comparing(Entry::getKey)).map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", "))
         + ")";
      return (proxy, method, args) -> {
         String name = method.getName();
         if ("annotationType".equals(name) && method.getParameterCount() == 0) {
            return annotationType;
         } else if ("equals".equals(name) && method.getParameterCount() == 1 && Object.class.equals(method.getParameterTypes()[0])) {
            return !(args[0] instanceof Annotation that) ? false : annotationType.equals(that.annotationType()) && valuesEqual(annotationType, proxy, that);
         } else if ("hashCode".equals(name) && method.getParameterCount() == 0) {
            return hashCode;
         } else if ("toString".equals(name) && method.getParameterCount() == 0) {
            return toString;
         } else if (method.getDeclaringClass() == annotationType) {
            return getValue.apply(method);
         } else {
            throw new IllegalStateException("Unknown method " + method + " for annotation type " + annotationType);
         }
      };
   }

   private static int memberHash(String name, Object value) {
      return 127 * name.hashCode() ^ valueHash(value);
   }

   private static int valueHash(Object value) {
      Class<? extends Object> valueClass = (Class<? extends Object>)value.getClass();
      return !valueClass.isArray()
         ? value.hashCode()
         : Unchecked.<Integer>supplier(
               () -> (Integer)MethodHandles.publicLookup()
                  .findStatic(Arrays.class, "hashCode", MethodType.methodType(int.class, valueClass))
                  .invoke((Object)value)
            )
            .get();
   }

   private static <A extends Annotation> boolean valuesEqual(Class<A> annotationType, Object a, Object b) {
      for (Method m : annotationType.getDeclaredMethods()) {
         Objects.requireNonNull(m);
         Object valueA = m;
         Function<Object, Object> invoker = Unchecked.function(x$0 -> valueA.invoke(x$0));
         valueA = invoker.apply(a);
         Object valueB = invoker.apply(b);
         if (valueA != null
            && valueB != null
            && valueA.getClass().isArray()
            && valueA.getClass().equals(valueB.getClass())
            && Boolean.FALSE
               .equals(
                  Unchecked.supplier(
                        () -> (Object)MethodHandles.publicLookup()
                           .findStatic(Arrays.class, "equals", MethodType.methodType(boolean.class, valueA.getClass(), valueB.getClass()))
                           .invoke((Object)a, (Object)b)
                     )
                     .get()
               )) {
            return false;
         }

         if (!Objects.equals(valueA, valueB)) {
            return false;
         }
      }

      return true;
   }
}
