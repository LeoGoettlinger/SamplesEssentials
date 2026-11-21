package de.theredend2000.advancedhunt.lib.cloud.util.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = API.Status.STABLE
)
public interface AnnotationAccessor {
   @API(
      status = API.Status.STABLE
   )
   @NonNull
   static AnnotationAccessor empty() {
      return new AnnotationAccessor.NullAnnotationAccessor();
   }

   @NonNull
   static AnnotationAccessor of(@NonNull final AnnotatedElement element) {
      return new AnnotatedElementAccessor(element);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   static AnnotationAccessor of(@NonNull final AnnotationAccessor... accessors) {
      return new MultiDelegateAnnotationAccessor(accessors);
   }

   @Nullable
   <A extends Annotation> A annotation(@NonNull Class<A> clazz);

   @NonNull
   Collection<Annotation> annotations();

   @API(
      status = API.Status.INTERNAL,
      consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
   )
   public static final class NullAnnotationAccessor implements AnnotationAccessor {
      @Nullable
      @Override
      public <A extends Annotation> A annotation(@NonNull final Class<A> clazz) {
         return null;
      }

      @NonNull
      @Override
      public Collection<Annotation> annotations() {
         return Collections.emptyList();
      }
   }
}
