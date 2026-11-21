package org.sethomegui.SetHomeGUI.libs.redis.jedis.providers;

import java.util.Collections;
import java.util.Map;
import org.sethomegui.SetHomeGUI.libs.pool2.PooledObjectFactory;
import org.sethomegui.SetHomeGUI.libs.pool2.impl.GenericObjectPoolConfig;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Connection;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.ConnectionFactory;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.ConnectionPool;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.HostAndPort;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.JedisClientConfig;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.annots.Experimental;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.csc.Cache;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.Pool;

public class PooledConnectionProvider implements ConnectionProvider {
   private final Pool<Connection> pool;
   private Object connectionMapKey = "";

   public PooledConnectionProvider(HostAndPort hostAndPort) {
      this(new ConnectionFactory(hostAndPort));
      this.connectionMapKey = hostAndPort;
   }

   public PooledConnectionProvider(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      this(new ConnectionPool(hostAndPort, clientConfig));
      this.connectionMapKey = hostAndPort;
   }

   @Experimental
   public PooledConnectionProvider(HostAndPort hostAndPort, JedisClientConfig clientConfig, Cache clientSideCache) {
      this(new ConnectionPool(hostAndPort, clientConfig, clientSideCache));
      this.connectionMapKey = hostAndPort;
   }

   public PooledConnectionProvider(HostAndPort hostAndPort, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig) {
      this(new ConnectionPool(hostAndPort, clientConfig, poolConfig));
      this.connectionMapKey = hostAndPort;
   }

   @Experimental
   public PooledConnectionProvider(
      HostAndPort hostAndPort, JedisClientConfig clientConfig, Cache clientSideCache, GenericObjectPoolConfig<Connection> poolConfig
   ) {
      this(new ConnectionPool(hostAndPort, clientConfig, clientSideCache, poolConfig));
      this.connectionMapKey = hostAndPort;
   }

   public PooledConnectionProvider(PooledObjectFactory<Connection> factory) {
      this(new ConnectionPool(factory));
      this.connectionMapKey = factory;
   }

   public PooledConnectionProvider(PooledObjectFactory<Connection> factory, GenericObjectPoolConfig<Connection> poolConfig) {
      this(new ConnectionPool(factory, poolConfig));
      this.connectionMapKey = factory;
   }

   private PooledConnectionProvider(Pool<Connection> pool) {
      this.pool = pool;
   }

   @Override
   public void close() {
      this.pool.close();
   }

   public final Pool<Connection> getPool() {
      return this.pool;
   }

   @Override
   public Connection getConnection() {
      return this.pool.getResource();
   }

   @Override
   public Connection getConnection(CommandArguments args) {
      return this.pool.getResource();
   }

   @Override
   public Map<?, Pool<Connection>> getConnectionMap() {
      return Collections.singletonMap(this.connectionMapKey, this.pool);
   }
}
