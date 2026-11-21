package org.enginehub.piston.inject;

import com.google.auto.value.AutoValue;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;

@AutoValue
public abstract class Key<T> {
   public static <T> Key<T> of(Class<T> clazz) {
      return of(TypeToken.of(clazz));
   }

   public static <T> Key<T> of(TypeToken<T> typeToken) {
      return of(typeToken, NullAnnotationWrapper.getInstance());
   }

   public static <T> Key<T> of(Class<T> clazz, @Nullable Annotation annotation) {
      return of(TypeToken.of(clazz), annotation);
   }

   public static <T> Key<T> of(Class<T> clazz, @Nullable Class<? extends Annotation> annotation) {
      return of(TypeToken.of(clazz), annotation);
   }

   public static <T> Key<T> of(TypeToken<T> typeToken, @Nullable Annotation annotation) {
      return of(typeToken, strategyFor(annotation));
   }

   public static <T> Key<T> of(TypeToken<T> typeToken, @Nullable Class<? extends Annotation> annotation) {
      return of(typeToken, strategyFor(annotation));
   }

   private static <T> Key<T> of(TypeToken<T> typeToken, AnnotationWrapper annotationWrapper) {
      return new AutoValue_Key<>(typeToken.wrap(), annotationWrapper);
   }

   private static AnnotationWrapper strategyFor(@Nullable Annotation annotation) {
      if (annotation == null) {
         return NullAnnotationWrapper.getInstance();
      } else {
         validateAnnotationType(annotation.annotationType());
         return (AnnotationWrapper)(annotation.annotationType().getDeclaredMethods().length == 0
            ? TypeAnnotationWrapper.from(annotation.annotationType())
            : InstanceAnnotationWrapper.from(annotation));
      }
   }

   private static AnnotationWrapper strategyFor(@Nullable Class<? extends Annotation> annotationType) {
      if (annotationType == null) {
         return NullAnnotationWrapper.getInstance();
      } else {
         validateAnnotationType(annotationType);
         return (AnnotationWrapper)(annotationType.getDeclaredMethods().length > 0
               && Stream.of(annotationType.getDeclaredMethods()).map(Method::getDefaultValue).allMatch(Objects::nonNull)
            ? strategyFor(Annotations.allDefaultsAnnotation(annotationType))
            : TypeAnnotationWrapper.from(annotationType));
      }
   }

   private static void validateAnnotationType(Class<? extends Annotation> annotationType) {
      Retention retention = annotationType.getAnnotation(Retention.class);
      Preconditions.checkArgument(
         retention != null && retention.value() == RetentionPolicy.RUNTIME, "Annotation type %s is not retained at runtime.", annotationType
      );
      InjectAnnotation injectAnnotation = annotationType.getAnnotation(InjectAnnotation.class);
      Preconditions.checkArgument(injectAnnotation != null, "Annotation type %s is not an inject annotation", annotationType);
   }

   public abstract TypeToken<T> getTypeToken();

   abstract AnnotationWrapper getAnnotationWrapper();

   @Nullable
   public final Annotation getAnnotation() {
      return this.getAnnotationWrapper().getAnnotation();
   }

   @Nullable
   public final Class<? extends Annotation> getAnnotationType() {
      return this.getAnnotationWrapper().getAnnotationType();
   }

   @Override
   public final String toString() {
      return MoreObjects.toStringHelper(Key.class).add("type", this.getTypeToken()).add("annotationWrapper", this.getAnnotationWrapper()).toString();
   }
}
