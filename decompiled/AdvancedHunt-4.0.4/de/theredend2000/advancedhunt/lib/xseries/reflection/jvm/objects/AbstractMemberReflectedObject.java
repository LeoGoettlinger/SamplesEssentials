package de.theredend2000.advancedhunt.lib.xseries.reflection.jvm.objects;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import org.jspecify.annotations.NonNull;

abstract class AbstractMemberReflectedObject extends AbstractReflectedObject {
   @NonNull
   @Override
   public abstract AnnotatedElement unreflect();

   protected abstract Member member();

   @NonNull
   @Override
   public String name() {
      return this.member().getName();
   }

   @Override
   public final Class<?> getDeclaringClass() {
      return this.member().getDeclaringClass();
   }

   @Override
   public final int getModifiers() {
      return this.member().getModifiers();
   }
}
