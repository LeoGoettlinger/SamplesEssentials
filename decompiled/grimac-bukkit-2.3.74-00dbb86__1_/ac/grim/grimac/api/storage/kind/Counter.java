package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.EnumSet;

@ApiStatus.Experimental
public record Counter<K>(
   @NotNull String name,
   @NotNull Class<K> keyType,
   @NotNull Codec<K> keyCodec,
   @NotNull EnumSet<Capability> requiredCapabilities,
   @NotNull EnumSet<Capability> optionalCapabilities
) implements DataKind<CounterEvent<K>, Long> {
   public Counter(
      @NotNull String name,
      @NotNull Class<K> keyType,
      @NotNull Codec<K> keyCodec,
      @NotNull EnumSet<Capability> requiredCapabilities,
      @NotNull EnumSet<Capability> optionalCapabilities
   ) {
      requiredCapabilities = EnumSet.copyOf(requiredCapabilities);
      optionalCapabilities = EnumSet.copyOf(optionalCapabilities);
      this.name = name;
      this.keyType = keyType;
      this.keyCodec = keyCodec;
      this.requiredCapabilities = requiredCapabilities;
      this.optionalCapabilities = optionalCapabilities;
   }

   @NotNull
   @Override
   public Class<CounterEvent<K>> eventType() {
      return CounterEvent.class;
   }

   @NotNull
   @Override
   public Class<Long> recordType() {
      return Long.class;
   }

   @NotNull
   @Override
   public Codec<Long> codec() {
      throw new UnsupportedOperationException("Counter records are primitive longs; no codec");
   }

   @NotNull
   public static <K> Counter.Builder<K> builder() {
      return new Counter.Builder<>();
   }

   public static final class Builder<K> {
      private String name;
      private Class<K> keyType;
      private Codec<K> keyCodec;
      private final EnumSet<Capability> required = EnumSet.of(Capability.KIND_COUNTER);
      private final EnumSet<Capability> optional = EnumSet.noneOf(Capability.class);

      @NotNull
      public Counter.Builder<K> name(@NotNull String name) {
         this.name = name;
         return this;
      }

      @NotNull
      public Counter.Builder<K> key(@NotNull Class<K> t, @NotNull Codec<K> c) {
         this.keyType = t;
         this.keyCodec = c;
         return this;
      }

      @NotNull
      public Counter.Builder<K> requireCapability(@NotNull Capability... caps) {
         for (Capability c : caps) {
            this.required.add(c);
         }

         return this;
      }

      @NotNull
      public Counter<K> build() {
         if (this.name == null) {
            throw new IllegalStateException("name");
         } else if (this.keyType != null && this.keyCodec != null) {
            return new Counter<>(this.name, this.keyType, this.keyCodec, this.required, this.optional);
         } else {
            throw new IllegalStateException("key");
         }
      }
   }
}
