package de.theredend2000.advancedhunt.lib.cloud.key;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = API.Status.INTERNAL
)
public final class SimpleMutableCloudKeyContainer implements MutableCloudKeyContainer {
   private final Map<CloudKey<?>, Object> map;

   public SimpleMutableCloudKeyContainer(final Map<CloudKey<?>, Object> map) {
      this.map = map;
   }

   @NonNull
   @Override
   public <V> Optional<V> optional(@NonNull final CloudKey<V> key) {
      return Optional.ofNullable((V)this.map.get(key));
   }

   @NonNull
   @Override
   public <V> Optional<V> optional(@NonNull final String key) {
      return this.optional((CloudKey<V>)CloudKey.of(key));
   }

   @Override
   public boolean contains(@NonNull final CloudKey<?> key) {
      return this.map.containsKey(key);
   }

   @NonNull
   @Override
   public Map<CloudKey<?>, ? extends Object> all() {
      return Collections.unmodifiableMap(this.map);
   }

   @Override
   public <V> void store(@NonNull final CloudKey<V> key, @NonNull final V value) {
      this.map.put(key, value);
   }

   @Override
   public <V> void store(@NonNull final String key, @NonNull final V value) {
      this.map.put(CloudKey.of(key), value);
   }

   @Override
   public void remove(@NonNull final CloudKey<?> key) {
      this.map.remove(key);
   }

   @Override
   public <V> V computeIfAbsent(@NonNull final CloudKey<V> key, @NonNull final Function<CloudKey<V>, V> defaultFunction) {
      return (V)this.map.computeIfAbsent(key, $ -> defaultFunction.apply(key));
   }

   @Nullable
   public <V> V getOrNull(final CloudKey<V> key) {
      return (V)this.map.get(key);
   }
}
