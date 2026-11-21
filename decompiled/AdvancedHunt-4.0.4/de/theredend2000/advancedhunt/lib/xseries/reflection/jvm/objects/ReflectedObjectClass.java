package de.theredend2000.advancedhunt.lib.xseries.reflection.jvm.objects;

import org.jspecify.annotations.NonNull;

final class ReflectedObjectClass extends AbstractReflectedObject {
   private final Class<?> delegate;

   ReflectedObjectClass(Class<?> delegate) {
      this.delegate = delegate;
   }

   @NonNull
   @Override
   public ReflectedObject.Type type() {
      return ReflectedObject.Type.CLASS;
   }

   @NonNull
   public Class<?> unreflect() {
      return this.delegate;
   }

   @NonNull
   @Override
   public String name() {
      return this.delegate.getSimpleName();
   }

   @Override
   public Class<?> getDeclaringClass() {
      return this.delegate.getDeclaringClass();
   }

   @Override
   public int getModifiers() {
      return this.delegate.getModifiers();
   }
}
