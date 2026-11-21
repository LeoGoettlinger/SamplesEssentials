package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RepresentationHint<V> {
   public static <V> RepresentationHint<V> of(final String identifier, final Class<V> valueType) {
      return builder().identifier(identifier).valueType(valueType).build();
   }

   public static <V> RepresentationHint<V> of(final String identifier, final TypeToken<V> valueType) {
      return builder().identifier(identifier).valueType(valueType).build();
   }

   public static <V> RepresentationHint.Builder<V> builder() {
      return new AutoValue_RepresentationHint.Builder<V>().inheritable(true);
   }

   RepresentationHint() {
   }

   public abstract String identifier();

   public abstract TypeToken<V> valueType();

   @Nullable
   public abstract V defaultValue();

   public abstract boolean inheritable();

   @com.google.auto.value.AutoValue.Builder
   public abstract static class Builder<V> {
      Builder() {
      }

      public abstract RepresentationHint.Builder<V> identifier(String identifier);

      public final RepresentationHint.Builder<V> valueType(final Class<V> valueType) {
         return this.valueType(TypeToken.get(valueType));
      }

      public abstract RepresentationHint.Builder<V> valueType(TypeToken<V> valueType);

      public abstract RepresentationHint.Builder<V> defaultValue(@Nullable V defaultValue);

      public abstract RepresentationHint.Builder<V> inheritable(boolean inheritable);

      public abstract RepresentationHint<V> build();
   }
}
