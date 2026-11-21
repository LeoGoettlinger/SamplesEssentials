package de.exlll.configlib;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Reflect {
   private static final Set<Class<?>> SIMPLE_TARGET_TYPES = Set.of(Boolean.class, Long.class, Double.class, String.class);
   private static final Map<Class<?>, Object> DEFAULT_VALUES = initDefaultValues();

   private Reflect() {
   }

   private static Map<Class<?>, Object> initDefaultValues() {
      return Stream.of(boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class)
         .collect(Collectors.toMap(type -> (Class<?>)type, type -> Array.get(Array.newInstance((Class<?>)type, 1), 0)));
   }

   static <T> T getDefaultValue(Class<T> clazz) {
      return (T)DEFAULT_VALUES.get(clazz);
   }

   static <T> T callConstructor(Class<T> cls, Class<?>[] argumentTypes, Object... arguments) {
      try {
         Constructor<T> constructor = cls.getDeclaredConstructor(argumentTypes);
         constructor.setAccessible(true);
         return constructor.newInstance(arguments);
      } catch (NoSuchMethodException var5) {
         String msg = "Type '%s' doesn't have a constructor with parameters: %s.".formatted(cls.getSimpleName(), argumentTypeNamesJoined(argumentTypes));
         throw new RuntimeException(msg, var5);
      } catch (IllegalAccessException var6) {
         throw new RuntimeException(var6);
      } catch (InstantiationException var7) {
         String msgx = "Type '%s' is not instantiable.".formatted(cls.getSimpleName());
         throw new RuntimeException(msgx, var7);
      } catch (InvocationTargetException var8) {
         String msgxx = "Constructor of type '%s' with parameters '%s' threw an exception."
            .formatted(cls.getSimpleName(), argumentTypeNamesJoined(argumentTypes));
         throw new RuntimeException(msgxx, var8);
      }
   }

   private static String argumentTypeNamesJoined(Class<?>[] argumentTypes) {
      return Arrays.stream(argumentTypes).map(Class::getName).collect(Collectors.joining(", "));
   }

   static boolean hasConstructor(Class<?> type, Class<?>... argumentTypes) {
      Predicate<Constructor<?>> predicate = ctor -> Arrays.equals((Object[])ctor.getParameterTypes(), (Object[])argumentTypes);
      return Arrays.stream(type.getDeclaredConstructors()).anyMatch(predicate);
   }

   static <T> T callNoParamConstructor(Class<T> cls) {
      try {
         Constructor<T> constructor = cls.getDeclaredConstructor();
         constructor.setAccessible(true);
         return constructor.newInstance();
      } catch (NoSuchMethodException var3) {
         String msg = "Type '%s' doesn't have a no-args constructor.".formatted(cls.getSimpleName());
         throw new RuntimeException(msg, var3);
      } catch (IllegalAccessException var4) {
         String msgx = "No-args constructor of type '%s' not accessible.".formatted(cls.getSimpleName());
         throw new RuntimeException(msgx, var4);
      } catch (InstantiationException var5) {
         String msgxx = "Type '%s' is not instantiable.".formatted(cls.getSimpleName());
         throw new RuntimeException(msgxx, var5);
      } catch (InvocationTargetException var6) {
         String msgxxx = "No-args constructor of type '%s' threw an exception.".formatted(cls.getSimpleName());
         throw new RuntimeException(msgxxx, var6);
      }
   }

   static <R> R callCanonicalConstructor(Class<R> recordType, Object... constructorArguments) {
      try {
         Constructor<R> constructor = getCanonicalConstructor(Validator.requireRecord(recordType));
         constructor.setAccessible(true);
         return constructor.newInstance(constructorArguments);
      } catch (NoSuchMethodException var4) {
         throw new RuntimeException(var4);
      } catch (IllegalAccessException var5) {
         throw new RuntimeException(var5);
      } catch (InstantiationException var6) {
         throw new RuntimeException(var6);
      } catch (InvocationTargetException var7) {
         String msg = "The canonical constructor of record type '%s' threw an exception.".formatted(recordType.getSimpleName());
         throw new RuntimeException(msg, var7);
      }
   }

   static <R> R callCanonicalConstructorWithDefaultValues(Class<R> recordType) {
      Object[] args = Arrays.stream(recordParameterTypes(Validator.requireRecord(recordType))).map(Reflect::getDefaultValue).toArray(Object[]::new);
      return callCanonicalConstructor(recordType, args);
   }

   static <R> Constructor<R> getCanonicalConstructor(Class<R> recordType) throws NoSuchMethodException {
      Class<?>[] parameterTypes = recordParameterTypes(Validator.requireRecord(recordType));
      return recordType.getDeclaredConstructor(parameterTypes);
   }

   private static <R> Class<?>[] recordParameterTypes(Class<R> recordType) {
      return Arrays.stream(recordType.getRecordComponents()).map(RecordComponent::getType).toArray(Class[]::new);
   }

   static <T> T[] newArray(Class<T> componentType, int length) {
      return (T[])((Object[])Array.newInstance(componentType, length));
   }

   static boolean hasDefaultConstructor(Class<?> type) {
      return Arrays.stream(type.getDeclaredConstructors()).anyMatch(ctor -> ctor.getParameterCount() == 0);
   }

   static Object getValue(Field field, Object instance) {
      try {
         field.setAccessible(true);
         return field.get(instance);
      } catch (IllegalAccessException var4) {
         String msg = "Illegal access of field '%s' on object %s.".formatted(field, instance);
         throw new RuntimeException(msg, var4);
      }
   }

   static Object getValue(RecordComponent component, Object recordInstance) {
      Method accessor = component.getAccessor();

      try {
         accessor.setAccessible(true);
         return accessor.invoke(recordInstance);
      } catch (IllegalAccessException var5) {
         String msg = "Illegal access of method '%s' on record '%s'.".formatted(accessor, recordInstance);
         throw new RuntimeException(msg, var5);
      } catch (InvocationTargetException var6) {
         String msgx = "Invocation of method '%s' on record '%s' failed.".formatted(accessor, recordInstance);
         throw new RuntimeException(msgx, var6);
      }
   }

   static void setValue(Field field, Object instance, Object value) {
      try {
         field.setAccessible(true);
         field.set(instance, value);
      } catch (IllegalAccessException var5) {
         String msg = "Illegal access of field '%s' on object %s.".formatted(field, instance);
         throw new RuntimeException(msg, var5);
      }
   }

   static boolean isIntegerType(Class<?> cls) {
      return cls == byte.class
         || cls == Byte.class
         || cls == short.class
         || cls == Short.class
         || cls == int.class
         || cls == Integer.class
         || cls == long.class
         || cls == Long.class;
   }

   static boolean isFloatingPointType(Class<?> cls) {
      return cls == float.class || cls == Float.class || cls == double.class || cls == Double.class;
   }

   static boolean isEnumType(Class<?> cls) {
      return cls.isEnum();
   }

   static boolean isArrayType(Class<?> cls) {
      return cls.isArray();
   }

   static boolean isListType(Class<?> cls) {
      return List.class.isAssignableFrom(cls);
   }

   static boolean isSetType(Class<?> cls) {
      return Set.class.isAssignableFrom(cls);
   }

   static boolean isMapType(Class<?> cls) {
      return Map.class.isAssignableFrom(cls);
   }

   static boolean isConfigurationClass(Class<?> cls) {
      return cls.getAnnotation(Configuration.class) != null;
   }

   static boolean isConfigurationType(Class<?> type) {
      return type.isRecord() || type.getAnnotation(Configuration.class) != null;
   }

   static boolean isIgnored(Field field) {
      return field.getAnnotation(Ignore.class) != null;
   }

   static Class<?> getClassByName(String className) {
      try {
         return Class.forName(className);
      } catch (ClassNotFoundException var2) {
         throw new RuntimeException(var2);
      }
   }

   static Object invoke(Method method, Object object, Object... arguments) {
      try {
         method.setAccessible(true);
         return method.invoke(object, arguments);
      } catch (InvocationTargetException | IllegalAccessException var4) {
         throw new RuntimeException(var4);
      }
   }

   static boolean isSimpleTargetType(Class<?> cls) {
      return SIMPLE_TARGET_TYPES.contains(cls);
   }
}
