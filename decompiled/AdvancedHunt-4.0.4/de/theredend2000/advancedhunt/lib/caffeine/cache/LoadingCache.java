package de.theredend2000.advancedhunt.lib.caffeine.cache;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface LoadingCache<K, V> extends Cache<K, V> {
   V get(K key);

   Map<K, V> getAll(Iterable<? extends K> keys);

   @CanIgnoreReturnValue
   CompletableFuture<V> refresh(K key);

   @CanIgnoreReturnValue
   CompletableFuture<Map<K, V>> refreshAll(Iterable<? extends K> keys);
}
