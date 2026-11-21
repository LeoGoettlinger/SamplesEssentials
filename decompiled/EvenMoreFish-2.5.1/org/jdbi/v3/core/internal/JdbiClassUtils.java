package org.jdbi.v3.core.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.internal.exceptions.Sneaky;
import org.jdbi.v3.core.mapper.reflect.ReflectionMappers;

public final class JdbiClassUtils {
   public static final Method EQUALS_METHOD = methodLookup(Object.class, "equals", Object.class);
   public static final Method HASHCODE_METHOD = methodLookup(Object.class, "hashCode");
   public static final Method TOSTRING_METHOD = methodLookup(Object.class, "toString");
   private static final Lookup LOOKUP = MethodHandles.lookup();
   private static final Object[] NO_ARGS = new Object[0];
   private static final Class<?>[] NO_PARAMS = new Class[0];
   private static final ConcurrentMap<Class<?>, MethodHandle> METHOD_HANDLE_CACHE = new ConcurrentHashMap<>();

   private JdbiClassUtils() {
      throw new UtilityClassException();
   }

   public static boolean isPresent(String klass) {
      try {
         Class.forName(klass);
         return true;
      } catch (ReflectiveOperationException var2) {
         return false;
      }
   }

   public static Method methodLookup(Class<?> klass, String methodName, Class<?>... parameterTypes) {
      try {
         return klass.getMethod(methodName, parameterTypes);
      } catch (SecurityException | ReflectiveOperationException var6) {
         try {
            return klass.getDeclaredMethod(methodName, parameterTypes);
         } catch (SecurityException | ReflectiveOperationException var5) {
            var6.addSuppressed(var5);
            throw new IllegalStateException(String.format("can't find %s#%s%s", klass.getName(), methodName, Arrays.asList(parameterTypes)), var6);
         }
      }
   }

   public static Optional<Method> safeMethodLookup(Class<?> klass, String methodName, Class<?>... parameterTypes) {
      try {
         return Optional.of(klass.getMethod(methodName, parameterTypes));
      } catch (SecurityException | ReflectiveOperationException var6) {
         try {
            return Optional.of(klass.getDeclaredMethod(methodName, parameterTypes));
         } catch (SecurityException | ReflectiveOperationException var5) {
            return Optional.empty();
         }
      }
   }

   public static Stream<Class<?>> superTypes(Class<?> type) {
      Class<?>[] interfaces = type.getInterfaces();
      Set<Class<?>> result = Stream.concat(Arrays.stream(interfaces).flatMap(JdbiClassUtils::superTypes), Arrays.stream(interfaces))
         .collect(Collectors.toSet());
      return result.stream();
   }

   public static Object[] safeVarargs(Object... args) {
      return args == null ? NO_ARGS : args;
   }

   public static <T> T checkedCreateInstance(Class<T> type) {
      return checkedCreateInstance(type, NO_PARAMS);
   }

   public static <T> T checkedCreateInstance(Class<T> type, Class<?>[] parameters, Object... values) {
      try {
         MethodHandle methodHandle = MethodHandles.lookup().findConstructor(type, MethodType.methodType(void.class, parameters));
         methodHandle = methodHandle.asType(MethodType.methodType(type, parameters));
         return (T)methodHandle.invokeWithArguments(values);
      } catch (Throwable var4) {
         throw Sneaky.throwAnyway(var4);
      }
   }

   public static <T> T findConstructorAndCreateInstance(Class<T> type, Class<?>[] types, JdbiClassUtils.MethodHandleInvoker invoker) {
      try {
         MethodHandle ctorHandle = METHOD_HANDLE_CACHE.computeIfAbsent(
            type, tx -> findCtorMethodHandleForParameters(tx, tx.getConstructors(), LOOKUP::unreflectConstructor, types)
         );
         return (T)invoker.createInstance(ctorHandle);
      } catch (Throwable var4) {
         throw Sneaky.throwAnyway(var4);
      }
   }

   public static <T> JdbiClassUtils.MethodHandleHolder<T> findConstructor(Class<T> type, Class<?>... types) {
      return holderFor(findCtorMethodHandleForParameters(type, type.getConstructors(), LOOKUP::unreflectConstructor, types));
   }

   public static <T> JdbiClassUtils.MethodHandleHolder<T> findConstructor(ConfigRegistry config, Class<T> type, Class<?>... types) {
      return holderFor(
         findCtorMethodHandleForParameters(
            type, type.getDeclaredConstructors(), constructor -> accessibleHandle(config, constructor, LOOKUP::unreflectConstructor), types
         )
      );
   }

   public static MethodHandle unreflect(ConfigRegistry config, Method method) {
      try {
         return accessibleHandle(config, method, LOOKUP::unreflect);
      } catch (IllegalAccessException var3) {
         throw new IllegalArgumentException(var3.getMessage(), var3);
      }
   }

   public static MethodHandle unreflectConstructor(ConfigRegistry config, Constructor<?> constructor) {
      try {
         return accessibleHandle(config, constructor, LOOKUP::unreflectConstructor);
      } catch (IllegalAccessException var3) {
         throw new IllegalArgumentException(var3.getMessage(), var3);
      }
   }

   private static <M extends Executable> MethodHandle accessibleHandle(ConfigRegistry config, M member, JdbiClassUtils.Unreflector<M> unreflector) throws IllegalAccessException {
      try {
         return unreflector.unreflect(member);
      } catch (IllegalAccessException var8) {
         try {
            return unreflector.unreflect(config.get(ReflectionMappers.class).makeAccessible(copyOf(member)));
         } catch (InaccessibleObjectException | SecurityException | IllegalAccessException var7) {
            Class<?> declaringClass = member.getDeclaringClass();
            IllegalAccessException failure = new IllegalAccessException(
               String.format(
                  "Jdbi can not access %s. Make %s and the member public, or enable the ReflectionMappers accessible object strategy%s.",
                  member,
                  declaringClass.getName(),
                  openPackageHint(declaringClass)
               )
            );
            failure.initCause(var7);
            failure.addSuppressed(var8);
            throw failure;
         }
      }
   }

   private static String openPackageHint(Class<?> type) {
      if (!type.getModule().isNamed()) {
         return "";
      } else {
         Module jdbi = JdbiClassUtils.class.getModule();
         return String.format(" and open package %s to %s", type.getPackageName(), jdbi.isNamed() ? "module " + jdbi.getName() : "ALL-UNNAMED");
      }
   }

   private static <M extends Executable> M copyOf(M member) {
      Class<?> declaringClass = member.getDeclaringClass();
      Executable[] candidates = (Executable[])(member instanceof Method ? declaringClass.getDeclaredMethods() : declaringClass.getDeclaredConstructors());
      return (M)Arrays.stream(candidates)
         .filter(member::equals)
         .findFirst()
         .orElseThrow(() -> new IllegalStateException(String.format("can't find %s on %s", member, declaringClass.getName())));
   }

   private static <T> JdbiClassUtils.MethodHandleHolder<T> holderFor(MethodHandle ctorHandle) {
      return invoker -> {
         try {
            return (T)invoker.createInstance(ctorHandle);
         } catch (Throwable var3) {
            throw Sneaky.throwAnyway(var3);
         }
      };
   }

   private static MethodHandle findCtorMethodHandleForParameters(
      Class<?> type, Constructor<?>[] constructors, JdbiClassUtils.Unreflector<Constructor<?>> unreflector, Class<?>... types
   ) {
      Deque<Throwable> accessFailures = new ArrayDeque<>();

      for (int argCount = types.length; argCount >= 0; argCount--) {
         for (Constructor<?> constructor : constructors) {
            if (parametersMatch(constructor, argCount, types)) {
               try {
                  MethodHandle methodHandle = unreflector.unreflect(constructor);
                  if (argCount < types.length) {
                     methodHandle = MethodHandles.dropArguments(methodHandle, argCount, Arrays.copyOfRange(types, argCount, types.length));
                  }

                  return methodHandle.asType(methodHandle.type().changeReturnType(Object.class));
               } catch (IllegalAccessException var11) {
                  accessFailures.add(var11);
               }
            }
         }
      }

      NoSuchMethodException failure = new NoSuchMethodException(
         String.format("No constructor for class '%s', loosely matching arguments %s", type.getName(), Arrays.toString((Object[])types))
      );
      if (!accessFailures.isEmpty()) {
         failure.initCause(accessFailures.removeFirst());
      }

      accessFailures.forEach(failure::addSuppressed);
      return MethodHandles.dropArguments(MethodHandles.insertArguments(MethodHandles.throwException(Object.class, Exception.class), 0, failure), 0, types);
   }

   private static boolean parametersMatch(Constructor<?> constructor, int argCount, Class<?>[] types) {
      if (constructor.getParameterCount() != argCount) {
         return false;
      } else {
         Class<?>[] parameterTypes = constructor.getParameterTypes();

         for (int i = 0; i < argCount; i++) {
            if (!parameterTypes[i].isAssignableFrom(types[i])) {
               return false;
            }
         }

         return true;
      }
   }

   @FunctionalInterface
   public interface MethodHandleHolder<T> {
      T invoke(JdbiClassUtils.MethodHandleInvoker invoker);
   }

   @FunctionalInterface
   public interface MethodHandleInvoker {
      Object createInstance(MethodHandle handle) throws Throwable;
   }

   public static final class MethodKey {
      public final String name;
      public final MethodType type;

      public MethodKey(String name, MethodType type) {
         this.name = name;
         this.type = type;
      }

      public static JdbiClassUtils.MethodKey methodKey(Method method) {
         return new JdbiClassUtils.MethodKey(method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.name, this.type);
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof JdbiClassUtils.MethodKey other) ? false : this.name.equals(other.name) && this.type.equals(other.type);
      }

      @Override
      public String toString() {
         return "MethodKey[" + this.name + "(" + this.type.parameterList().stream().map(Class::toString).collect(Collectors.joining(",")) + ")]";
      }
   }

   @FunctionalInterface
   private interface Unreflector<M> {
      MethodHandle unreflect(M member) throws IllegalAccessException;
   }
}
