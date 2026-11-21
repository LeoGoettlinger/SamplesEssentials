package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Experimental
public record EventStream<E, R>(
   @NotNull String name,
   @NotNull Class<E> eventType,
   @NotNull Supplier<E> newEvent,
   @NotNull Class<R> recordType,
   @NotNull Codec<R> codec,
   @NotNull Function<E, R> eventToRecord,
   @NotNull String timestampField,
   @NotNull List<String> partitionFields,
   @Nullable Duration retention,
   @NotNull Granularity granularity,
   @NotNull EnumSet<Capability> requiredCapabilities,
   @NotNull EnumSet<Capability> optionalCapabilities
) implements DataKind<E, R> {
   public EventStream(
      @NotNull String name,
      @NotNull Class<E> eventType,
      @NotNull Supplier<E> newEvent,
      @NotNull Class<R> recordType,
      @NotNull Codec<R> codec,
      @NotNull Function<E, R> eventToRecord,
      @NotNull String timestampField,
      @NotNull List<String> partitionFields,
      @Nullable Duration retention,
      @NotNull Granularity granularity,
      @NotNull EnumSet<Capability> requiredCapabilities,
      @NotNull EnumSet<Capability> optionalCapabilities
   ) {
      partitionFields = List.copyOf(partitionFields);
      requiredCapabilities = EnumSet.copyOf(requiredCapabilities);
      optionalCapabilities = EnumSet.copyOf(optionalCapabilities);
      this.name = name;
      this.eventType = eventType;
      this.newEvent = newEvent;
      this.recordType = recordType;
      this.codec = codec;
      this.eventToRecord = eventToRecord;
      this.timestampField = timestampField;
      this.partitionFields = partitionFields;
      this.retention = retention;
      this.granularity = granularity;
      this.requiredCapabilities = requiredCapabilities;
      this.optionalCapabilities = optionalCapabilities;
   }

   @NotNull
   public static <E, R> EventStream.Builder<E, R> builder() {
      return new EventStream.Builder<>();
   }

   public static final class Builder<E, R> {
      private String name;
      private Class<E> eventType;
      private Supplier<E> newEvent;
      private Class<R> recordType;
      private Codec<R> codec;
      private Function<E, R> eventToRecord;
      private String timestampField;
      private List<String> partitionFields = List.of();
      private Duration retention;
      private Granularity granularity = Granularity.SECONDS;
      private final EnumSet<Capability> required = EnumSet.of(Capability.KIND_EVENT_STREAM);
      private final EnumSet<Capability> optional = EnumSet.noneOf(Capability.class);

      @NotNull
      public EventStream.Builder<E, R> name(@NotNull String name) {
         this.name = name;
         return this;
      }

      @NotNull
      public EventStream.Builder<E, R> event(@NotNull Class<E> t, @NotNull Supplier<E> f) {
         this.eventType = t;
         this.newEvent = f;
         return this;
      }

      @NotNull
      public EventStream.Builder<E, R> record(@NotNull Class<R> t) {
         this.recordType = t;
         return this;
      }

      @NotNull
      public EventStream.Builder<E, R> codec(@NotNull Codec<R> c) {
         this.codec = c;
         return this;
      }

      @NotNull
      public EventStream.Builder<E, R> eventToRecord(@NotNull Function<E, R> f) {
         this.eventToRecord = f;
         return this;
      }

      @NotNull
      public EventStream.Builder<E, R> timestamp(@NotNull String field) {
         this.timestampField = field;
         return this;
      }

      @NotNull
      public EventStream.Builder<E, R> partition(@NotNull String... fields) {
         this.partitionFields = List.of(fields);
         return this;
      }

      @NotNull
      public EventStream.Builder<E, R> retention(@Nullable Duration d) {
         this.retention = d;
         return this;
      }

      @NotNull
      public EventStream.Builder<E, R> granularity(@NotNull Granularity g) {
         this.granularity = g;
         return this;
      }

      @NotNull
      public EventStream.Builder<E, R> requireCapability(@NotNull Capability... caps) {
         for (Capability c : caps) {
            this.required.add(c);
         }

         return this;
      }

      @NotNull
      public EventStream.Builder<E, R> optionalCapability(@NotNull Capability... caps) {
         for (Capability c : caps) {
            this.optional.add(c);
         }

         return this;
      }

      @NotNull
      public EventStream<E, R> build() {
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
         } else if (this.timestampField == null) {
            throw new IllegalStateException("timestamp");
         } else {
            return new EventStream<>(
               this.name,
               this.eventType,
               this.newEvent,
               this.recordType,
               this.codec,
               this.eventToRecord,
               this.timestampField,
               this.partitionFields,
               this.retention,
               this.granularity,
               this.required,
               this.optional
            );
         }
      }
   }
}
