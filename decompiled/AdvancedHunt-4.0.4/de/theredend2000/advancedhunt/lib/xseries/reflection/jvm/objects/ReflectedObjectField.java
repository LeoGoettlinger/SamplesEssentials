package de.theredend2000.advancedhunt.lib.xseries.reflection.jvm.objects;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import org.jspecify.annotations.NonNull;

final class ReflectedObjectField extends AbstractMemberReflectedObject {
   private final Field delegate;

   ReflectedObjectField(Field delegate) {
      this.delegate = delegate;
   }

   @NonNull
   @Override
   public ReflectedObject.Type type() {
      return ReflectedObject.Type.FIELD;
   }

   @NonNull
   public Field unreflect() {
      return this.delegate;
   }

   @Override
   protected Member member() {
      return this.delegate;
   }
}
