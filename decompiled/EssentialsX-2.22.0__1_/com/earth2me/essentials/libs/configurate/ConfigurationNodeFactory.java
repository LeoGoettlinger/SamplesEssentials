package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.util.CheckedConsumer;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.util.Map.Entry;
import java.util.stream.Collector;

@FunctionalInterface
public interface ConfigurationNodeFactory<N extends ConfigurationNode> {
   default ConfigurationOptions defaultOptions() {
      return ConfigurationOptions.defaults();
   }

   N createNode(ConfigurationOptions options);

   default N createNode() {
      return this.createNode(this.defaultOptions());
   }

   default <E extends Exception> N createNode(final CheckedConsumer<N, E> action) throws E {
      N node = this.createNode();
      action.accept(node);
      return node;
   }

   default <E extends Exception> N createNode(final ConfigurationOptions options, final CheckedConsumer<N, E> action) throws E {
      N node = this.createNode(options);
      action.accept(node);
      return node;
   }

   default <V> Collector<Entry<?, V>, N, N> toMapCollector(final TypeToken<V> valueType) {
      return Collector.of(this::createNode, (node, entry) -> {
         try {
            node.node(entry.getKey()).set(valueType, entry.getValue());
         } catch (SerializationException var4) {
            throw new IllegalArgumentException(var4);
         }
      }, (a, b) -> {
         a.mergeFrom(b);
         return a;
      });
   }

   default <V> Collector<Entry<?, V>, N, N> toMapCollector(final Class<V> valueType) {
      return Collector.of(this::createNode, (node, entry) -> {
         try {
            node.node(entry.getKey()).set(valueType, entry.getValue());
         } catch (SerializationException var4) {
            throw new IllegalArgumentException(var4);
         }
      }, (a, b) -> {
         a.mergeFrom(b);
         return a;
      });
   }

   default <V> Collector<V, N, N> toListCollector(final TypeToken<V> valueType) {
      return Collector.of(this::createNode, (node, value) -> {
         try {
            node.appendListNode().set(valueType, value);
         } catch (SerializationException var4) {
            throw new IllegalArgumentException(var4);
         }
      }, (a, b) -> {
         a.mergeFrom(b);
         return a;
      });
   }

   default <V> Collector<V, N, N> toListCollector(final Class<V> valueType) {
      return Collector.of(this::createNode, (node, value) -> {
         try {
            node.appendListNode().set(valueType, value);
         } catch (SerializationException var4) {
            throw new IllegalArgumentException(var4);
         }
      }, (a, b) -> {
         a.mergeFrom(b);
         return a;
      });
   }
}
