package com.earth2me.essentials.libs.configurate.util;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import com.earth2me.essentials.libs.geantyref.TypeFactory;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Types {
   private static final Map<Type, Type> BOXED_TO_PRIMITIVE = UnmodifiableCollections.buildMap(m -> {
      m.put(Boolean.class, boolean.class);
      m.put(Character.class, char.class);
      m.put(Byte.class, byte.class);
      m.put(Short.class, short.class);
      m.put(Integer.class, int.class);
      m.put(Long.class, long.class);
      m.put(Float.class, float.class);
      m.put(Double.class, double.class);
      m.put(Void.class, void.class);
   });
   private static final Map<Type, Type> PRIMITIVE_TO_BOXED = UnmodifiableCollections.buildMap(m -> {
      m.put(boolean.class, Boolean.class);
      m.put(char.class, Character.class);
      m.put(byte.class, Byte.class);
      m.put(short.class, Short.class);
      m.put(int.class, Integer.class);
      m.put(long.class, Long.class);
      m.put(float.class, Float.class);
      m.put(double.class, Double.class);
      m.put(void.class, Void.class);
   });

   private Types() {
   }

   public static boolean isArray(final Type input) {
      if (input instanceof Class) {
         return ((Class)input).isArray();
      } else {
         return input instanceof ParameterizedType ? isArray(((ParameterizedType)input).getRawType()) : input instanceof GenericArrayType;
      }
   }

   public static boolean isBoxedPrimitive(final Type input) {
      return BOXED_TO_PRIMITIVE.containsKey(input);
   }

   public static Type unbox(final Type input) {
      Type ret = BOXED_TO_PRIMITIVE.get(input);
      return ret == null ? input : ret;
   }

   @Nullable
   public static Object defaultValue(final Class<?> type) {
      Objects.requireNonNull(type, "type");
      if (!type.isPrimitive() || type == void.class) {
         return null;
      } else if (type == boolean.class) {
         return Boolean.FALSE;
      } else if (type == char.class) {
         return '\u0000';
      } else if (type == byte.class) {
         return (byte)0;
      } else if (type == short.class) {
         return (short)0;
      } else if (type == int.class) {
         return 0;
      } else if (type == long.class) {
         return 0L;
      } else if (type == float.class) {
         return 0.0F;
      } else {
         return type == double.class ? 0.0 : Array.get(Array.newInstance(type, 1), 0);
      }
   }

   public static Type box(final Type input) {
      Type ret = PRIMITIVE_TO_BOXED.get(input);
      return ret == null ? input : ret;
   }

   public static <T> TypeToken<List<T>> makeList(final Class<T> elementType) {
      return (TypeToken<List<T>>)TypeToken.get(TypeFactory.parameterizedClass(List.class, elementType));
   }

   public static <T> TypeToken<List<T>> makeListType(final TypeToken<T> elementType) {
      return (TypeToken<List<T>>)TypeToken.get(TypeFactory.parameterizedClass(List.class, elementType.getType()));
   }

   public static <K, V> TypeToken<Map<K, V>> makeMap(final Class<K> key, final Class<V> value) {
      return (TypeToken<Map<K, V>>)TypeToken.get(TypeFactory.parameterizedClass(Map.class, key, value));
   }

   public static <K, V> TypeToken<Map<K, V>> makeMap(final TypeToken<K> key, final TypeToken<V> value) {
      return (TypeToken<Map<K, V>>)TypeToken.get(TypeFactory.parameterizedClass(Map.class, key.getType(), value.getType()));
   }

   public static <K, V> TypeToken<Map<K, V>> makeMap(final Class<K> key, final TypeToken<V> value) {
      return (TypeToken<Map<K, V>>)TypeToken.get(TypeFactory.parameterizedClass(Map.class, key, value.getType()));
   }

   public static <K, V> TypeToken<Map<K, V>> makeMap(final TypeToken<K> key, final Class<V> value) {
      return (TypeToken<Map<K, V>>)TypeToken.get(TypeFactory.parameterizedClass(Map.class, key.getType(), value));
   }

   public static AnnotatedElement combinedAnnotations(final AnnotatedElement... elements) {
      return new Types.CombinedAnnotations(Arrays.copyOf(elements, elements.length));
   }

   public static Type requireCompleteParameters(final Type input) {
      if (GenericTypeReflector.isMissingTypeParameters(input)) {
         throw new IllegalArgumentException("Provided type " + input + " is a raw type, which is not accepted.");
      } else {
         return input;
      }
   }

   public static Stream<Type> allSuperTypes(final Type type) {
      return calculateSuperTypes(type, false);
   }

   public static Stream<Type> allSuperTypesAndInterfaces(final Type type) {
      return calculateSuperTypes(type, true);
   }

   private static Stream<Type> calculateSuperTypes(final Type type, final boolean includeInterfaces) {
      Objects.requireNonNull(type, "type");
      return StreamSupport.stream(Spliterators.spliterator(new Types.SuperTypesIterator(type, includeInterfaces), Long.MAX_VALUE, 1280), false);
   }

   public static List<Method> allDeclaredMethods(final Class<?> clazz) {
      List<Method> seenMethods = new ArrayList<>();
      Set<String> seenSignatures = new HashSet<>();
      Deque<Class<?>> typesToVisit = new ArrayDeque<>();
      typesToVisit.add(clazz);

      Class<?> pointer;
      while ((pointer = typesToVisit.poll()) != null) {
         for (Method method : pointer.getDeclaredMethods()) {
            StringBuilder descBuilder = new StringBuilder(method.getName());
            descBuilder.append('(');

            for (Class<?> param : method.getParameterTypes()) {
               descBuilder.append(param.getName()).append(';');
            }

            descBuilder.append(')').append(method.getReturnType().getName());
            String desc = descBuilder.toString();
            if (seenSignatures.add(desc)) {
               seenMethods.add(method);
            }
         }

         Class<?> superclass = pointer.getSuperclass();
         if (superclass != null && !Object.class.equals(superclass)) {
            typesToVisit.add(superclass);
         }

         Collections.addAll(typesToVisit, pointer.getInterfaces());
      }

      return seenMethods;
   }

   static class CombinedAnnotations implements AnnotatedElement {
      private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
      private final AnnotatedElement[] elements;

      CombinedAnnotations(final AnnotatedElement[] elements) {
         this.elements = elements;
      }

      @Override
      public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
         for (AnnotatedElement element : this.elements) {
            if (element.isAnnotationPresent(annotationClass)) {
               return true;
            }
         }

         return false;
      }

      @Override
      public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
         T ret = null;

         for (AnnotatedElement element : this.elements) {
            ret = element.getAnnotation(annotationClass);
            if (ret != null) {
               break;
            }
         }

         return ret;
      }

      @Override
      public Annotation[] getAnnotations() {
         List<Annotation> annotations = new ArrayList<>();

         for (AnnotatedElement element : this.elements) {
            Annotation[] annotation = element.getAnnotations();
            if (annotation.length > 0) {
               annotations.addAll(Arrays.asList(annotation));
            }
         }

         return annotations.toArray(EMPTY_ANNOTATION_ARRAY);
      }

      @Override
      public <T extends Annotation> T[] getAnnotationsByType(final Class<T> annotationClass) {
         List<T> annotations = new ArrayList<>();

         for (AnnotatedElement element : this.elements) {
            T[] annotation = element.getAnnotationsByType(annotationClass);
            if (annotation.length > 0) {
               annotations.addAll(Arrays.asList(annotation));
            }
         }

         return (T[])annotations.toArray(EMPTY_ANNOTATION_ARRAY);
      }

      @Override
      public <T extends Annotation> T getDeclaredAnnotation(final Class<T> annotationClass) {
         T ret = null;

         for (AnnotatedElement element : this.elements) {
            ret = element.getDeclaredAnnotation(annotationClass);
            if (ret != null) {
               break;
            }
         }

         return ret;
      }

      @Override
      public <T extends Annotation> T[] getDeclaredAnnotationsByType(final Class<T> annotationClass) {
         List<T> annotations = new ArrayList<>();

         for (AnnotatedElement element : this.elements) {
            T[] annotation = element.getDeclaredAnnotationsByType(annotationClass);
            if (annotation.length > 0) {
               annotations.addAll(Arrays.asList(annotation));
            }
         }

         return (T[])annotations.toArray(EMPTY_ANNOTATION_ARRAY);
      }

      @Override
      public Annotation[] getDeclaredAnnotations() {
         List<Annotation> annotations = new ArrayList<>();

         for (AnnotatedElement element : this.elements) {
            Annotation[] annotation = element.getDeclaredAnnotations();
            if (annotation.length > 0) {
               annotations.addAll(Arrays.asList(annotation));
            }
         }

         return annotations.toArray(EMPTY_ANNOTATION_ARRAY);
      }
   }

   static class SuperTypesIterator implements Iterator<Type> {
      private final boolean includeInterfaces;
      private final Deque<Type> types = new ArrayDeque<>();
      private final Set<Type> seen = new HashSet<>();

      SuperTypesIterator(final Type base, final boolean includeInterfaces) {
         this.types.add(base);
         this.includeInterfaces = includeInterfaces;
      }

      @Override
      public boolean hasNext() {
         return !this.types.isEmpty();
      }

      public Type next() {
         Type head = this.types.removeLast();
         if ((!(head instanceof Class) || !((Class)head).isArray()) && !(head instanceof GenericArrayType)) {
            if (!(head instanceof Class) && !(head instanceof ParameterizedType)) {
               if (head instanceof TypeVariable) {
                  this.addAllIfUnseen(head, ((TypeVariable)head).getBounds());
               } else if (head instanceof WildcardType) {
                  Type[] upperBounds = ((WildcardType)head).getUpperBounds();
                  if (upperBounds.length == 1) {
                     Type upperBound = upperBounds[0];
                     this.addSuperClassAndInterface(head, GenericTypeReflector.erase(upperBound), TypeFactory::wildcardExtends);
                  } else {
                     this.addAllIfUnseen(head, ((WildcardType)head).getUpperBounds());
                  }
               }
            } else {
               Class<?> clazz;
               if (head instanceof ParameterizedType) {
                  ParameterizedType parameterized = (ParameterizedType)head;
                  clazz = (Class<?>)parameterized.getRawType();
               } else {
                  clazz = (Class<?>)head;
               }

               this.addSuperClassAndInterface(head, clazz, null);
            }
         } else {
            Type componentType;
            if (head instanceof Class) {
               componentType = ((Class)head).getComponentType();
            } else {
               componentType = ((GenericArrayType)head).getGenericComponentType();
            }

            this.addSuperClassAndInterface(componentType, GenericTypeReflector.erase(componentType), TypeFactory::arrayOf);
         }

         return head;
      }

      private void addAllIfUnseen(final Type base, final Type... types) {
         for (Type type : types) {
            this.addIfUnseen(GenericTypeReflector.resolveType(type, base));
         }
      }

      private void addIfUnseen(final Type type) {
         if (this.seen.add(type)) {
            this.types.addLast(type);
         }
      }

      private void addSuperClassAndInterface(final Type base, final Class<?> actualClass, @Nullable final UnaryOperator<Type> postProcess) {
         if (this.includeInterfaces) {
            for (Type itf : actualClass.getGenericInterfaces()) {
               if (postProcess != null) {
                  this.addIfUnseen(postProcess.apply(GenericTypeReflector.resolveType(itf, base)));
               } else {
                  this.addIfUnseen(GenericTypeReflector.resolveType(itf, base));
               }
            }
         }

         if (actualClass.getSuperclass() != null) {
            Type resolved = GenericTypeReflector.resolveType(actualClass.getGenericSuperclass(), base);
            this.addIfUnseen(postProcess == null ? resolved : postProcess.apply(resolved));
         }
      }
   }
}
