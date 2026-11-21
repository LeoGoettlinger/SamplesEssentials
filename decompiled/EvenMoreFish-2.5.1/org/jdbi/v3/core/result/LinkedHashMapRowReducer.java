package org.jdbi.v3.core.result;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

@FunctionalInterface
public interface LinkedHashMapRowReducer<K, V> extends RowReducer<Map<K, V>, V> {
   default Map<K, V> container() {
      return new LinkedHashMap<>();
   }

   default Stream<V> stream(Map<K, V> container) {
      return container.values().stream();
   }
}
