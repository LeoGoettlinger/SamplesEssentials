package de.oliver.cloud.parser.aggregate;

import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.key.CloudKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

final class AggregateParsingContextImpl<C> implements AggregateParsingContext<C> {
   private final Map<CloudKey<?>, Object> storage = new HashMap<>();
   private final Collection<String> validKeys;

   AggregateParsingContextImpl(@NonNull final AggregateParser<C, ?> parser) {
      this.validKeys = parser.components().stream().map(CommandComponent::name).collect(Collectors.toList());
   }

   @Override
   public <V> void store(@NonNull final CloudKey<V> key, @NonNull final V value) {
      this.storage.put(key, value);
   }

   @Override
   public <V> void store(@NonNull final String key, @NonNull final V value) {
      this.storage.put(CloudKey.of(key), value);
   }

   @Override
   public void remove(@NonNull final CloudKey<?> key) {
      this.storage.remove(key);
   }

   @Override
   public <V> V computeIfAbsent(@NonNull final CloudKey<V> key, @NonNull final Function<CloudKey<V>, V> defaultFunction) {
      return (V)this.storage.computeIfAbsent(key, k -> defaultFunction.apply((CloudKey<V>)k));
   }

   @NonNull
   @Override
   public <V> Optional<V> optional(@NonNull final CloudKey<V> key) {
      Object value = this.storage.get(key);
      return value != null ? Optional.of((V)value) : Optional.empty();
   }

   @NonNull
   @Override
   public <V> Optional<V> optional(@NonNull final String key) {
      Object value = this.storage.get(CloudKey.of(key));
      return value != null ? Optional.of((V)value) : Optional.empty();
   }

   @NonNull
   @Override
   public <V> V get(@NonNull final CloudKey<V> key) {
      if (!this.validKeys.contains(key.name())) {
         throw new NullPointerException("No value with the given key has been stored in the context");
      } else {
         return Objects.requireNonNull((V)this.storage.get(key));
      }
   }

   @NonNull
   @Override
   public <V> V get(@NonNull final String key) {
      if (!this.validKeys.contains(key)) {
         throw new NullPointerException("No value with the given key has been stored in the context");
      } else {
         return Objects.requireNonNull((V)this.storage.get(CloudKey.of(key)));
      }
   }

   @Override
   public boolean contains(@NonNull final CloudKey<?> key) {
      return this.storage.containsKey(key);
   }

   @Override
   public boolean contains(@NonNull final String key) {
      return this.storage.containsKey(CloudKey.of(key));
   }

   @NonNull
   @Override
   public Map<CloudKey<?>, ? extends Object> all() {
      return this.storage;
   }
}
