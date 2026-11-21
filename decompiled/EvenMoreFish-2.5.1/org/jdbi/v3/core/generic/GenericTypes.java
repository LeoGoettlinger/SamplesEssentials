package org.jdbi.v3.core.generic;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeFactory;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import org.jdbi.v3.core.internal.UtilityClassException;

public class GenericTypes {
   private static final TypeVariable<Class<Map>> KEY;
   private static final TypeVariable<Class<Map>> VALUE;

   private GenericTypes() {
      throw new UtilityClassException();
   }

   public static Class<?> getErasedType(Type type) {
      return GenericTypeReflector.erase(type);
   }

   public static Optional<Type> findGenericParameter(Type type, Class<?> parameterizedSupertype) {
      return findGenericParameter(type, parameterizedSupertype, 0);
   }

   public static Optional<Type> findGenericParameter(Type type, Class<?> parameterizedSupertype, int n) {
      return type instanceof Class<?> clazz && clazz.getGenericSuperclass() instanceof ParameterizedType pt && pt.getRawType() == parameterizedSupertype
         ? Optional.of(pt.getActualTypeArguments()[n])
         : Optional.ofNullable(GenericTypeReflector.getTypeParameter(type, parameterizedSupertype.getTypeParameters()[n]));
   }

   public static Type resolveType(Type type, Type contextType) {
      return GenericTypeReflector.resolveType(type, contextType);
   }

   public static boolean isArray(Type type) {
      return type instanceof Class<?> clazz && clazz.isArray();
   }

   public static Type arrayType(Type type) {
      return TypeFactory.arrayOf(type);
   }

   public static Type resolveMapEntryType(Type mapType) {
      Type keyType = resolveType(KEY, mapType);
      Type valueType = resolveType(VALUE, mapType);
      return resolveMapEntryType(keyType, valueType);
   }

   public static Type resolveMapEntryType(Type keyType, Type valueType) {
      return TypeFactory.parameterizedClass(Entry.class, keyType, valueType);
   }

   public static Type parameterizeClass(Class<?> clazz, Type... arguments) {
      return TypeFactory.parameterizedClass(clazz, arguments);
   }

   public static Type box(Type type) {
      return GenericTypeReflector.box(type);
   }

   public static boolean isSuperType(Type superType, Type subType) {
      return GenericTypeReflector.isSuperType(superType, subType);
   }

   static {
      TypeVariable<Class<Map>>[] mapParams = Map.class.getTypeParameters();
      KEY = mapParams[0];
      VALUE = mapParams[1];
   }
}
