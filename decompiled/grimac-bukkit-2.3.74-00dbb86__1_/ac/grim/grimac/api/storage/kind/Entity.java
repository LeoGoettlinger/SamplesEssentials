package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Experimental
public record Entity<ID, E, R>(
   @NotNull String name,
   @NotNull Class<E> eventType,
   @NotNull Supplier<E> newEvent,
   @NotNull Class<R> recordType,
   @NotNull Codec<R> codec,
   @NotNull Function<E, R> eventToRecord,
   @NotNull Class<ID> idType,
   @NotNull Function<R, ID> idOf,
   @NotNull List<IndexSpec> secondaryIndexes,
   @NotNull EnumSet<Capability> requiredCapabilities,
   @NotNull EnumSet<Capability> optionalCapabilities
) implements DataKind<E, R> {
   public Entity(
      @NotNull String name,
      @NotNull Class<E> eventType,
      @NotNull Supplier<E> newEvent,
      @NotNull Class<R> recordType,
      @NotNull Codec<R> codec,
      @NotNull Function<E, R> eventToRecord,
      @NotNull Class<ID> idType,
      @NotNull Function<R, ID> idOf,
      @NotNull List<IndexSpec> secondaryIndexes,
      @NotNull EnumSet<Capability> requiredCapabilities,
      @NotNull EnumSet<Capability> optionalCapabilities
   ) {
      secondaryIndexes = List.copyOf(secondaryIndexes);
      requiredCapabilities = EnumSet.copyOf(requiredCapabilities);
      optionalCapabilities = EnumSet.copyOf(optionalCapabilities);
      this.name = name;
      this.eventType = eventType;
      this.newEvent = newEvent;
      this.recordType = recordType;
      this.codec = codec;
      this.eventToRecord = eventToRecord;
      this.idType = idType;
      this.idOf = idOf;
      this.secondaryIndexes = secondaryIndexes;
      this.requiredCapabilities = requiredCapabilities;
      this.optionalCapabilities = optionalCapabilities;
   }

   @NotNull
   public static <ID, E, R> Entity.Builder<ID, E, R> builder() {
      return new Entity.Builder<>();
   }

   public static final class Builder<ID, E, R> {
      private String name;
      private Class<E> eventType;
      private Supplier<E> newEvent;
      private Class<R> recordType;
      private Codec<R> codec;
      private Function<E, R> eventToRecord;
      private Class<ID> idType;
      private Function<R, ID> idOf;
      private final List<IndexSpec> indexes = new ArrayList<>();
      private final EnumSet<Capability> required = EnumSet.of(Capability.KIND_ENTITY);
      private final EnumSet<Capability> optional = EnumSet.noneOf(Capability.class);

      @NotNull
      public Entity.Builder<ID, E, R> name(@NotNull String name) {
         this.name = name;
         return this;
      }

      @NotNull
      public Entity.Builder<ID, E, R> event(@NotNull Class<E> t, @NotNull Supplier<E> f) {
         this.eventType = t;
         this.newEvent = f;
         return this;
      }

      @NotNull
      public Entity.Builder<ID, E, R> record(@NotNull Class<R> t) {
         this.recordType = t;
         return this;
      }

      @NotNull
      public Entity.Builder<ID, E, R> codec(@NotNull Codec<R> c) {
         this.codec = c;
         return this;
      }

      @NotNull
      public Entity.Builder<ID, E, R> eventToRecord(@NotNull Function<E, R> f) {
         this.eventToRecord = f;
         return this;
      }

      @NotNull
      public Entity.Builder<ID, E, R> id(@NotNull Class<ID> t, @NotNull Function<R, ID> f) {
         this.idType = t;
         this.idOf = f;
         return this;
      }

      @NotNull
      public Entity.Builder<ID, E, R> secondaryIndex(@NotNull IndexSpec spec) {
         this.indexes.add(spec);
         return this;
      }

      @NotNull
      public Entity.Builder<ID, E, R> secondaryIndex(@NotNull String name, @NotNull String... fields) {
         this.indexes.add(IndexSpec.of(name, fields));
         return this;
      }

      @NotNull
      public Entity.Builder<ID, E, R> requireCapability(@NotNull Capability... caps) {
         for (Capability c : caps) {
            this.required.add(c);
         }

         return this;
      }

      @NotNull
      public Entity.Builder<ID, E, R> optionalCapability(@NotNull Capability... caps) {
         for (Capability c : caps) {
            this.optional.add(c);
         }

         return this;
      }

      @NotNull
      public Entity<ID, E, R> build() {
         if (this.name == null) {
            throw new IllegalStateException("name");
         } else if (this.eventType == null || this.newEvent == null) {
            throw new IllegalStateException("event");
         } else if (this.recordType == null) {
            throw new IllegalStateException("record");
         } else if (this.codec == null) {
            throw new IllegalStateException("codec");
         } else if (this.eventToRecord == null) {
            throw new IllegalStateException("eventToRecord");
         } else if (this.idType != null && this.idOf != null) {
            return new Entity<>(
               this.name,
               this.eventType,
               this.newEvent,
               this.recordType,
               this.codec,
               this.eventToRecord,
               this.idType,
               this.idOf,
               this.indexes,
               this.required,
               this.optional
            );
         } else {
            throw new IllegalStateException("id");
         }
      }
   }
}
