package de.theredend2000.advancedhunt.lib.xseries.reflection.jvm.objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import org.jspecify.annotations.NonNull;

final class ReflectedObjectConstructor extends AbstractMemberReflectedObject {
   private final Constructor<?> delegate;

   ReflectedObjectConstructor(Constructor<?> delegate) {
      this.delegate = delegate;
   }

   @NonNull
   @Override
   public ReflectedObject.Type type() {
      return ReflectedObject.Type.CONSTRUCTOR;
   }

   @NonNull
   @Override
   public String name() {
      return "<init>";
   }

   @NonNull
   public Constructor<?> unreflect() {
      return this.delegate;
   }

   @Override
   protected Member member() {
      return this.delegate;
   }
}
