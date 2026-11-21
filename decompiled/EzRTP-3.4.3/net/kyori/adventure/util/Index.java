package net.kyori.adventure.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public final class Index<K, V> {
   private final Map<K, V> keyToValue;
   private final Map<V, K> valueToKey;

   public static <K, V extends Enum<V>> Index<K, V> create(final Class<V> type, final Function<? super V, ? extends K> keyFunction) {
      return create(type, keyFunction, type.getEnumConstants());
   }

   @SafeVarargs
   public static <K, V extends Enum<V>> Index<K, V> create(final Class<V> type, final Function<? super V, ? extends K> keyFunction, final V... values) {
      return create(List.of(values), length -> new EnumMap<>(type), keyFunction);
   }

   @SafeVarargs
   public static <K, V> Index<K, V> create(final Function<? super V, ? extends K> keyFunction, final V... values) {
      return create(List.of(values), (IntFunction<Map<V, K>>)(HashMap::new), keyFunction);
   }

   public static <K, V> Index<K, V> create(final Function<? super V, ? extends K> keyFunction, final List<V> constants) {
      return create(constants, (IntFunction<Map<V, K>>)(HashMap::new), keyFunction);
   }

   private static <K, V> Index<K, V> create(
      final List<V> values, final IntFunction<Map<V, K>> valueToKeyFactory, final Function<? super V, ? extends K> keyFunction
   ) {
      int length = values.size();
      Map<K, V> keyToValue = new HashMap<>(length);
      Map<V, K> valueToKey = valueToKeyFactory.apply(length);

      for (int i = 0; i < length; i++) {
         V value = values.get(i);
         K key = (K)keyFunction.apply(value);
         if (keyToValue.putIfAbsent(key, value) != null) {
            throw new IllegalStateException(String.format("Key %s already mapped to value %s", key, keyToValue.get(key)));
         }

         if (valueToKey.putIfAbsent(value, key) != null) {
            throw new IllegalStateException(String.format("Value %s already mapped to key %s", value, valueToKey.get(value)));
         }
      }

      return new Index<>(Map.copyOf(keyToValue), Map.copyOf(valueToKey));
   }

   private Index(final Map<K, V> keyToValue, final Map<V, K> valueToKey) {
      this.keyToValue = keyToValue;
      this.valueToKey = valueToKey;
   }

   public Set<K> keys() {
      return Set.copyOf(this.keyToValue.keySet());
   }

   @Nullable
   public K key(final V value) {
      return this.valueToKey.get(value);
   }

   public K keyOrThrow(final V value) {
      K key = this.key(value);
      if (key == null) {
         throw new NoSuchElementException("There is no key for value " + value);
      } else {
         return key;
      }
   }

   @Contract("_, null -> null; _, !null -> !null")
   @Nullable
   public K keyOr(final V value, @Nullable final K defaultKey) {
      K key = this.key(value);
      return key == null ? defaultKey : key;
   }

   public Set<V> values() {
      return Set.copyOf(this.valueToKey.keySet());
   }

   @Nullable
   public V value(final K key) {
      return this.keyToValue.get(key);
   }

   public V valueOrThrow(final K key) {
      V value = this.value(key);
      if (value == null) {
         throw new NoSuchElementException("There is no value for key " + key);
      } else {
         return value;
      }
   }

   @Contract("_, null -> null; _, !null -> !null")
   @Nullable
   public V valueOr(final K key, @Nullable final V defaultValue) {
      V value = this.value(key);
      return value == null ? defaultValue : value;
   }

   public Map<K, V> keyToValue() {
      return Map.copyOf(this.keyToValue);
   }

   public Map<V, K> valueToKey() {
      return Map.copyOf(this.valueToKey);
   }
}
