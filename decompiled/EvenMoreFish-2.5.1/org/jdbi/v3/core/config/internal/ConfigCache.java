package org.jdbi.v3.core.config.internal;

import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.Configurable;
import org.jdbi.v3.core.statement.StatementContext;

public interface ConfigCache<K, V> {
   V get(K key, ConfigRegistry config);

   default V get(K key, Configurable<?> configurable) {
      return this.get(key, configurable.getConfig());
   }

   default V get(K key, StatementContext ctx) {
      return this.get(key, ctx.getConfig());
   }
}
