package ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.option;

import ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.option.value.ValueType;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jspecify.annotations.Nullable;

final class OptionSchemaImpl implements OptionSchema {
   final OptionState emptyState;
   final ConcurrentMap<String, Option<?>> options = new ConcurrentHashMap<>();

   OptionSchemaImpl(@Nullable final OptionSchemaImpl parent) {
      if (parent != null) {
         this.options.putAll(parent.options);
      }

      this.emptyState = new OptionStateImpl(this, new IdentityHashMap<>());
   }

   @Override
   public Set<Option<?>> knownOptions() {
      return Collections.unmodifiableSet(new HashSet<>(this.options.values()));
   }

   @Override
   public boolean has(final Option<?> option) {
      Option<?> own = this.options.get(option.id());
      return own != null && own.equals(option);
   }

   @Override
   public OptionState.Builder stateBuilder() {
      return new OptionStateImpl.BuilderImpl(this);
   }

   @Override
   public OptionState.VersionedBuilder versionedStateBuilder() {
      return new OptionStateImpl.VersionedBuilderImpl(this);
   }

   @Override
   public OptionState emptyState() {
      return this.emptyState;
   }

   @Override
   public String toString() {
      return "OptionSchemaImpl{options=" + this.options + '}';
   }

   static final class Instances {
      static OptionSchemaImpl.MutableImpl GLOBAL = new OptionSchemaImpl(null).new MutableImpl();
   }

   final class MutableImpl implements OptionSchema.Mutable {
      <T> Option<T> register(final String id, final ValueType<T> type, @Nullable final T defaultValue) {
         Option<T> ret = new OptionImpl<>(Objects.requireNonNull(id, "id"), Objects.requireNonNull(type, "type"), defaultValue);
         if (OptionSchemaImpl.this.options.putIfAbsent(id, ret) != null) {
            throw new IllegalStateException("Key " + id + " has already been used. Option keys must be unique within a schema.");
         } else {
            return ret;
         }
      }

      @Override
      public Option<String> stringOption(final String id, @Nullable final String defaultValue) {
         return this.register(id, ValueType.stringType(), defaultValue);
      }

      @Override
      public Option<Boolean> booleanOption(final String id, final boolean defaultValue) {
         return this.register(id, ValueType.booleanType(), defaultValue);
      }

      @Override
      public Option<Integer> intOption(final String id, final int defaultValue) {
         return this.register(id, ValueType.integerType(), defaultValue);
      }

      @Override
      public Option<Double> doubleOption(final String id, final double defaultValue) {
         return this.register(id, ValueType.doubleType(), defaultValue);
      }

      @Override
      public <E extends Enum<E>> Option<E> enumOption(final String id, final Class<E> enumClazz, @Nullable final E defaultValue) {
         return this.register(id, ValueType.enumType(enumClazz), defaultValue);
      }

      @Override
      public OptionSchema frozenView() {
         return OptionSchemaImpl.this;
      }

      @Override
      public Set<Option<?>> knownOptions() {
         return OptionSchemaImpl.this.knownOptions();
      }

      @Override
      public boolean has(final Option<?> option) {
         return OptionSchemaImpl.this.has(option);
      }

      @Override
      public OptionState.Builder stateBuilder() {
         return OptionSchemaImpl.this.stateBuilder();
      }

      @Override
      public OptionState.VersionedBuilder versionedStateBuilder() {
         return OptionSchemaImpl.this.versionedStateBuilder();
      }

      @Override
      public OptionState emptyState() {
         return OptionSchemaImpl.this.emptyState();
      }

      @Override
      public String toString() {
         return "MutableImpl{schema=" + OptionSchemaImpl.this + "}";
      }
   }
}
