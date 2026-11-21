package org.jdbi.v3.core.qualifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.internal.AnnotationFactory;

public final class QualifiedType<T> {
   private final Type type;
   private final Set<QualifiedType.QualifierKey> qualifiers;
   private Set<Annotation> annotations;
   private int hashCode;

   public static <T> QualifiedType<T> of(Class<T> clazz) {
      return new QualifiedType<>(clazz, Collections.emptySet());
   }

   public static QualifiedType<?> of(Type type) {
      return new QualifiedType(type, Collections.emptySet());
   }

   public static <T> QualifiedType<T> of(GenericType<T> type) {
      return (QualifiedType<T>)of(type.getType());
   }

   private QualifiedType(Type type, Set<QualifiedType.QualifierKey> qualifiers) {
      this.type = type;
      this.qualifiers = qualifiers;
   }

   public QualifiedType<T> with(Annotation... newQualifiers) {
      return new QualifiedType<>(this.type, keysOf(Arrays.asList(newQualifiers)));
   }

   @SafeVarargs
   public final QualifiedType<T> with(Class<? extends Annotation>... newQualifiers) {
      return new QualifiedType<>(this.type, keysOfClasses(Arrays.asList(newQualifiers)));
   }

   public QualifiedType<T> withAnnotations(Iterable<? extends Annotation> newQualifiers) {
      return new QualifiedType<>(this.type, keysOf(newQualifiers));
   }

   public QualifiedType<T> withAnnotationClasses(Iterable<Class<? extends Annotation>> newQualifiers) {
      return new QualifiedType<>(this.type, keysOfClasses(newQualifiers));
   }

   private static Set<QualifiedType.QualifierKey> keysOf(Iterable<? extends Annotation> annotations) {
      List<QualifiedType.QualifierKey> keys = new ArrayList<>();

      for (Annotation annotation : annotations) {
         keys.add(QualifiedType.QualifierKey.of(annotation));
      }

      return Set.copyOf(keys);
   }

   private static Set<QualifiedType.QualifierKey> keysOfClasses(Iterable<Class<? extends Annotation>> annotationTypes) {
      List<QualifiedType.QualifierKey> keys = new ArrayList<>();

      for (Class<? extends Annotation> annotationType : annotationTypes) {
         keys.add(QualifiedType.QualifierKey.of(annotationType));
      }

      return Set.copyOf(keys);
   }

   public Type getType() {
      return this.type;
   }

   public Set<Annotation> getQualifiers() {
      Set<Annotation> result = this.annotations;
      if (result == null) {
         List<Annotation> collected = new ArrayList<>();

         for (QualifiedType.QualifierKey key : this.qualifiers) {
            collected.add(key.annotation());
         }

         result = Set.copyOf(collected);
         this.annotations = result;
      }

      return result;
   }

   public boolean hasQualifiers(Set<? extends Annotation> expected) {
      if (this.qualifiers.size() != expected.size()) {
         return false;
      } else {
         for (Annotation annotation : expected) {
            if (!this.qualifiers.contains(QualifiedType.QualifierKey.of(annotation))) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean hasNoQualifiers() {
      return this.qualifiers.isEmpty();
   }

   public QualifiedType<?> mapType(Function<Type, Type> mapper) {
      return new QualifiedType(mapper.apply(this.type), this.qualifiers);
   }

   public Optional<QualifiedType<?>> flatMapType(Function<Type, Optional<Type>> mapper) {
      return mapper.apply(this.type).map(mappedType -> new QualifiedType(mappedType, this.qualifiers));
   }

   public boolean hasQualifier(Class<? extends Annotation> qualifier) {
      for (QualifiedType.QualifierKey key : this.qualifiers) {
         if (key.type() == qualifier) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         QualifiedType<?> that = (QualifiedType<?>)o;
         return Objects.equals(this.type, that.type) && Objects.equals(this.qualifiers, that.qualifiers);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int h = this.hashCode;
      if (h == 0) {
         h = Objects.hash(this.type, this.qualifiers);
         this.hashCode = h;
      }

      return h;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      this.qualifiers.forEach(qualifier -> builder.append(qualifier).append(' '));
      builder.append(this.type.getTypeName());
      return builder.toString();
   }

   private record QualifierKey(Class<? extends Annotation> type, Annotation instance) {
      private static final ClassValue<Boolean> HAS_MEMBERS = new ClassValue<Boolean>() {
         protected Boolean computeValue(Class<?> annotationType) {
            return annotationType.getDeclaredMethods().length > 0;
         }
      };

      static QualifiedType.QualifierKey of(Annotation annotation) {
         Class<? extends Annotation> annotationType = annotation.annotationType();
         return new QualifiedType.QualifierKey(annotationType, hasMembers(annotationType) ? annotation : null);
      }

      static QualifiedType.QualifierKey of(Class<? extends Annotation> annotationType) {
         return new QualifiedType.QualifierKey(annotationType, hasMembers(annotationType) ? AnnotationFactory.create(annotationType) : null);
      }

      private static boolean hasMembers(Class<? extends Annotation> annotationType) {
         return HAS_MEMBERS.get(annotationType);
      }

      Annotation annotation() {
         return this.instance != null ? this.instance : AnnotationFactory.create(this.type);
      }

      @Override
      public String toString() {
         return this.instance != null ? this.instance.toString() : "@" + this.type.getName() + "()";
      }
   }
}
