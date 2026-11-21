package org.enginehub.piston.inject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.enginehub.piston.util.ValueProvider;

public final class MapBackedValueStore implements InjectedValueStore {
   private final Map<Key<?>, ValueProvider<InjectedValueAccess, ?>> providers;

   public static MapBackedValueStore create() {
      return create(new ConcurrentHashMap<>());
   }

   public static MapBackedValueStore create(Map<Key<?>, ValueProvider<InjectedValueAccess, ?>> providers) {
      return new MapBackedValueStore(providers);
   }

   private MapBackedValueStore(Map<Key<?>, ValueProvider<InjectedValueAccess, ?>> providers) {
      this.providers = providers;
   }

   @Override
   public <T> void injectValue(Key<T> key, ValueProvider<InjectedValueAccess, T> provider) {
      this.providers.put(key, provider);
   }

   @Override
   public <T> Optional<T> injectedValue(Key<T> key, InjectedValueAccess context) {
      ValueProvider<InjectedValueAccess, T> provider = (ValueProvider<InjectedValueAccess, T>)this.providers.get(key);
      return Optional.ofNullable(provider).flatMap(p -> p.value(context));
   }
}
