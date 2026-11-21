package de.theredend2000.advancedhunt.lib.xseries.reflection.jvm.objects;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import org.jspecify.annotations.NonNull;

final class ReflectedObjectMethod extends AbstractMemberReflectedObject {
   private final Method delegate;

   ReflectedObjectMethod(Method delegate) {
      this.delegate = delegate;
   }

   @NonNull
   @Override
   public ReflectedObject.Type type() {
      return ReflectedObject.Type.METHOD;
   }

   @NonNull
   public Method unreflect() {
      return this.delegate;
   }

   @Override
   protected Member member() {
      return this.delegate;
   }
}
