package com.viaversion.viaversion.libs.mcstructs.text.serializer.legacy;

import com.viaversion.viaversion.libs.mcstructs.snbt.SNbt;
import java.util.function.Predicate;

public class EventSerializer<R, T extends R, A extends Enum<A>, IO> {
   private final Predicate<R> classMatcher;
   private final EventSerializer.IOFunction<T, IO> serializer;
   private final A action;
   private final EventSerializer.IOFunction<IO, T> deserializer;

   protected EventSerializer(Predicate<R> classMatcher, EventSerializer.IOFunction<T, IO> serializer, A action, EventSerializer.IOFunction<IO, T> deserializer) {
      this.classMatcher = classMatcher;
      this.serializer = serializer;
      this.action = action;
      this.deserializer = deserializer;
   }

   public A getAction() {
      return this.action;
   }

   public boolean matches(R rawType) {
      return this.classMatcher.test(rawType);
   }

   public IO serialize(SNbt<?> sNbt, T value) throws Throwable {
      return this.serializer.apply(sNbt, value);
   }

   public boolean matches(A action) {
      return this.action == action;
   }

   public T deserialize(SNbt<?> sNbt, IO value) throws Throwable {
      return this.deserializer.apply(sNbt, value);
   }

   @FunctionalInterface
   protected interface BasicIOFunction<I, O> extends EventSerializer.IOFunction<I, O> {
      O apply(I var1) throws Throwable;

      @Override
      default O apply(SNbt<?> sNbt, I value) throws Throwable {
         return this.apply(value);
      }
   }

   @FunctionalInterface
   protected interface IOFunction<I, O> {
      O apply(SNbt<?> var1, I var2) throws Throwable;
   }
}
