package me.byteful.plugin.leveltools.libs.lamp.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class Primitives {
   private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER;
   private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE;

   private Primitives() {
   }

   public static Class<?> getType(@NotNull Object o) {
      return o instanceof Class ? (Class)o : o.getClass();
   }

   public static <T> Class<T> wrap(Class<T> type) {
      Preconditions.notNull(type, "type");
      Class<T> wrapped = (Class<T>)PRIMITIVE_TO_WRAPPER.get(type);
      return wrapped == null ? type : wrapped;
   }

   public static Type wrapType(Type type) {
      Preconditions.notNull(type, "type");
      if (!(type instanceof Class)) {
         return type;
      } else {
         Class<?> wrapped = PRIMITIVE_TO_WRAPPER.get(type);
         return (Type)(wrapped == null ? type : wrapped);
      }
   }

   public static <T> Class<T> unwrap(Class<T> type) {
      Preconditions.notNull(type, "type");
      Class<T> unwrapped = (Class<T>)WRAPPER_TO_PRIMITIVE.get(type);
      return unwrapped == null ? type : unwrapped;
   }

   public static boolean isWrapperType(Class<?> type) {
      Preconditions.notNull(type, "type");
      return WRAPPER_TO_PRIMITIVE.containsKey(type);
   }

   public static Class<?> getRawType(Type type) {
      if (type instanceof Class) {
         return (Class<?>)type;
      } else if (type instanceof ParameterizedType) {
         ParameterizedType parameterizedType = (ParameterizedType)type;
         Type rawType = parameterizedType.getRawType();
         if (!(rawType instanceof Class)) {
            throw new IllegalStateException("Expected a Class, found a " + rawType);
         } else {
            return (Class<?>)rawType;
         }
      } else if (type instanceof GenericArrayType) {
         Type componentType = ((GenericArrayType)type).getGenericComponentType();
         return Array.newInstance(getRawType(componentType), 0).getClass();
      } else if (type instanceof TypeVariable) {
         return Object.class;
      } else if (type instanceof WildcardType) {
         return getRawType(((WildcardType)type).getUpperBounds()[0]);
      } else {
         String className = type == null ? "null" : type.getClass().getName();
         throw new IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + className);
      }
   }

   public static Type getInsideGeneric(Type genericType, Type fallback) {
      try {
         return ((ParameterizedType)genericType).getActualTypeArguments()[0];
      } catch (ClassCastException var3) {
         return fallback;
      }
   }

   private static void add(Map<Class<?>, Class<?>> forward, Map<Class<?>, Class<?>> backward, Class<?> key, Class<?> value) {
      forward.put(key, value);
      backward.put(value, key);
   }

   static {
      Map<Class<?>, Class<?>> primToWrap = new LinkedHashMap<>(16);
      Map<Class<?>, Class<?>> wrapToPrim = new LinkedHashMap<>(16);
      add(primToWrap, wrapToPrim, boolean.class, Boolean.class);
      add(primToWrap, wrapToPrim, byte.class, Byte.class);
      add(primToWrap, wrapToPrim, char.class, Character.class);
      add(primToWrap, wrapToPrim, double.class, Double.class);
      add(primToWrap, wrapToPrim, float.class, Float.class);
      add(primToWrap, wrapToPrim, int.class, Integer.class);
      add(primToWrap, wrapToPrim, long.class, Long.class);
      add(primToWrap, wrapToPrim, short.class, Short.class);
      add(primToWrap, wrapToPrim, void.class, Void.class);
      PRIMITIVE_TO_WRAPPER = java.util.Collections.unmodifiableMap(primToWrap);
      WRAPPER_TO_PRIMITIVE = java.util.Collections.unmodifiableMap(wrapToPrim);
   }
}
