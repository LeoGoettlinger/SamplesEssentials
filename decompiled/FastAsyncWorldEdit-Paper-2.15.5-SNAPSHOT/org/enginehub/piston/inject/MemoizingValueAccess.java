package org.enginehub.piston.inject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.enginehub.piston.util.ValueProvider;

public final class MemoizingValueAccess implements InjectedValueAccess {
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private final Map<Key<?>, Optional<?>> memory = new HashMap<>();
   private final InjectedValueAccess delegate;

   public static MemoizingValueAccess wrap(InjectedValueAccess delegate) {
      return delegate instanceof MemoizingValueAccess ? (MemoizingValueAccess)delegate : new MemoizingValueAccess(delegate);
   }

   private MemoizingValueAccess(InjectedValueAccess delegate) {
      this.delegate = delegate;
   }

   public InjectedValueAccess snapshotMemory() {
      Builder<Key<?>, ValueProvider<InjectedValueAccess, ?>> snapshot = ImmutableMap.builder();
      this.lock.readLock().lock();

      try {
         this.memory.forEach((k, v) -> snapshot.put(k, ValueProvider.constant(v.orElse(null))));
      } finally {
         this.lock.readLock().unlock();
      }

      return MapBackedValueStore.create(snapshot.build());
   }

   @Override
   public <T> Optional<T> injectedValue(Key<T> key, InjectedValueAccess context) {
      this.lock.readLock().lock();

      try {
         Optional<?> result = this.memory.get(key);
         if (result != null) {
            return (Optional<T>)result;
         }
      } finally {
         this.lock.readLock().unlock();
      }

      this.lock.writeLock().lock();

      Optional var4;
      try {
         Optional<?> result = this.memory.get(key);
         if (result == null) {
            result = this.delegate.injectedValue(key, context);
            this.memory.put(key, result);
         }

         var4 = result;
      } finally {
         this.lock.writeLock().unlock();
      }

      return var4;
   }
}
