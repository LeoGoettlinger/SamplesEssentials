package su.nightexpress.nightcore.bridge.key;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.registry.NRegistry;

@NullMarked
public class KeyedRegistry<T extends KeyHolder> implements NRegistry<AdaptedKey, T> {
   protected final Map<AdaptedKey, T> registryMap = new ConcurrentHashMap<>();

   @Override
   public void clear() {
      this.registryMap.clear();
   }

   @Override
   public int size() {
      return this.registryMap.size();
   }

   public void register(@NonNull T item) {
      this.registryMap.put(item.getKey(), item);
   }

   @Nullable
   public T remove(@NonNull T item) {
      return this.remove(item.getKey());
   }

   @Nullable
   public T remove(AdaptedKey id) {
      return this.registryMap.remove(id);
   }

   @Nullable
   public T get(AdaptedKey key) {
      return this.registryMap.get(key);
   }

   public Optional<T> lookup(AdaptedKey key) {
      return Optional.ofNullable(this.get(key));
   }

   @Override
   public Set<T> values() {
      return Set.copyOf(this.registryMap.values());
   }

   @Override
   public Set<AdaptedKey> keys() {
      return Set.copyOf(this.registryMap.keySet());
   }

   public Set<String> keyStrings() {
      return this.registryMap.keySet().stream().map(AdaptedKey::value).collect(Collectors.toSet());
   }

   @Override
   public Stream<T> stream() {
      return this.values().stream();
   }
}
