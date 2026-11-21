package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.EnumSet;

@ApiStatus.Experimental
public record KeyValueScoped<S, V>(
   @NotNull String name,
   @NotNull Class<S> scopeType,
   @NotNull Class<V> valueType,
   @NotNull Codec<V> valueCodec,
   @NotNull EnumSet<Capability> requiredCapabilities,
   @NotNull EnumSet<Capability> optionalCapabilities
) implements DataKind<KeyValueEvent<S, V>, KeyValueRecord<S, V>> {
   public KeyValueScoped(
      @NotNull String name,
      @NotNull Class<S> scopeType,
      @NotNull Class<V> valueType,
      @NotNull Codec<V> valueCodec,
      @NotNull EnumSet<Capability> requiredCapabilities,
      @NotNull EnumSet<Capability> optionalCapabilities
   ) {
      requiredCapabilities = EnumSet.copyOf(requiredCapabilities);
      optionalCapabilities = EnumSet.copyOf(optionalCapabilities);
      this.name = name;
      this.scopeType = scopeType;
      this.valueType = valueType;
      this.valueCodec = valueCodec;
      this.requiredCapabilities = requiredCapabilities;
      this.optionalCapabilities = optionalCapabilities;
   }

   @NotNull
   @Override
   public Class<KeyValueEvent<S, V>> eventType() {
      return KeyValueEvent.class;
   }

   @NotNull
   @Override
   public Class<KeyValueRecord<S, V>> recordType() {
      return KeyValueRecord.class;
   }

   @NotNull
   @Override
   public Codec<KeyValueRecord<S, V>> codec() {
      throw new UnsupportedOperationException("KeyValueScoped uses valueCodec per-entry; container codec is synthesized by the adapter");
   }

   @NotNull
   public static <S, V> KeyValueScoped.Builder<S, V> builder() {
      return new KeyValueScoped.Builder<>();
   }

   public static final class Builder<S, V> {
      private String name;
      private Class<S> scopeType;
      private Class<V> valueType;
      private Codec<V> valueCodec;
      private final EnumSet<Capability> required = EnumSet.of(Capability.KIND_KV_SCOPED);
      private final EnumSet<Capability> optional = EnumSet.noneOf(Capability.class);

      @NotNull
      public KeyValueScoped.Builder<S, V> name(@NotNull String name) {
         this.name = name;
         return this;
      }

      @NotNull
      public KeyValueScoped.Builder<S, V> scope(@NotNull Class<S> t) {
         this.scopeType = t;
         return this;
      }

      @NotNull
      public KeyValueScoped.Builder<S, V> value(@NotNull Class<V> t, @NotNull Codec<V> c) {
         this.valueType = t;
         this.valueCodec = c;
         return this;
      }

      @NotNull
      public KeyValueScoped.Builder<S, V> requireCapability(@NotNull Capability... caps) {
         for (Capability c : caps) {
            this.required.add(c);
         }

         return this;
      }

      @NotNull
      public KeyValueScoped.Builder<S, V> optionalCapability(@NotNull Capability... caps) {
         for (Capability c : caps) {
            this.optional.add(c);
         }

         return this;
      }

      @NotNull
      public KeyValueScoped<S, V> build() {
         if (this.name == null) {
            throw new IllegalStateException("name");
         } else if (this.scopeType == null) {
            throw new IllegalStateException("scope");
         } else if (this.valueType != null && this.valueCodec != null) {
            return new KeyValueScoped<>(this.name, this.scopeType, this.valueType, this.valueCodec, this.required, this.optional);
         } else {
            throw new IllegalStateException("value");
         }
      }
   }
}
