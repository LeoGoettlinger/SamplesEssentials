package io.github.retrooper.packetevents.adventure.option;

import io.github.retrooper.packetevents.adventure.option.value.ValueType;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.Nullable;

@NonExtendable
public interface Option<V> {
   @Deprecated
   static Option<Boolean> booleanOption(final String id, final boolean defaultValue) {
      return OptionSchema.globalSchema().booleanOption(id, defaultValue);
   }

   @Deprecated
   static <E extends Enum<E>> Option<E> enumOption(final String id, final Class<E> enumClazz, final E defaultValue) {
      return OptionSchema.globalSchema().enumOption(id, enumClazz, defaultValue);
   }

   String id();

   @Deprecated
   default Class<V> type() {
      return this.valueType().type();
   }

   ValueType<V> valueType();

   @Nullable
   V defaultValue();
}
