package com.viaversion.viaversion.libs.mcstructs.text.serializer.legacy;

import com.viaversion.viaversion.libs.mcstructs.snbt.SNbt;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SerializerMap<R, A extends Enum<A>, IO> {
   private final SNbt<?> sNbt;
   private final Map<A, List<EventSerializer<R, ? extends R, A, IO>>> serializers;
   private final Function<R, A> toActionFunction;

   public static <R, A extends Enum<A>, IO> SerializerMap.Builder<R, A, IO> create(SNbt<?> sNbt) {
      return new SerializerMap.Builder<>(sNbt);
   }

   private SerializerMap(SNbt<?> sNbt, List<EventSerializer<R, ? extends R, A, IO>> serializers, Function<R, A> toActionFunction) {
      this.sNbt = sNbt;
      this.toActionFunction = toActionFunction;
      Map<A, List<EventSerializer<R, ? extends R, A, IO>>> map = new HashMap<>();

      for (EventSerializer<R, ? extends R, A, IO> serializer : serializers) {
         map.computeIfAbsent(serializer.getAction(), k -> new ArrayList<>()).add(serializer);
      }

      this.serializers = new EnumMap<>(map);
   }

   public IO serialize(R event) {
      A action = this.toActionFunction.apply(event);
      List<EventSerializer<R, ? extends R, A, IO>> serializers = this.serializers.get(action);
      if (serializers != null && !serializers.isEmpty()) {
         for (EventSerializer<R, ? extends R, A, IO> serializer : serializers) {
            if (serializer.matches(event)) {
               try {
                  return serializer.serialize(this.sNbt, this.cast(event));
               } catch (Throwable var7) {
               }
            }
         }

         throw new UnsupportedOperationException("No serializer found for " + event);
      } else {
         throw new UnsupportedOperationException("No serializer found for " + event);
      }
   }

   public R deserialize(A action, IO value) {
      List<EventSerializer<R, ? extends R, A, IO>> serializers = this.serializers.get(action);
      if (serializers != null && !serializers.isEmpty()) {
         for (EventSerializer<R, ? extends R, A, IO> serializer : serializers) {
            if (serializer.matches(action)) {
               try {
                  return (R)serializer.deserialize(this.sNbt, value);
               } catch (Throwable var7) {
               }
            }
         }

         throw new UnsupportedOperationException("No serializer found for " + action + " - " + value);
      } else {
         return null;
      }
   }

   private <X> X cast(Object o) {
      return (X)o;
   }

   public static class Builder<R, A extends Enum<A>, IO> {
      private final SNbt<?> sNbt;
      private final List<EventSerializer<R, ? extends R, A, IO>> serializers = new ArrayList<>();

      protected Builder(SNbt<?> sNbt) {
         this.sNbt = sNbt;
      }

      public SerializerMap.Builder<R, A, IO> add(EventSerializer<R, ? extends R, A, IO> serializer) {
         this.serializers.add(serializer);
         return this;
      }

      public SerializerMap<R, A, IO> finalize(Function<R, A> toActionFunction) {
         return new SerializerMap<>(this.sNbt, this.serializers, toActionFunction);
      }
   }
}
