package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import org.sethomegui.SetHomeGUI.libs.pool2.PooledObjectFactory;
import org.sethomegui.SetHomeGUI.libs.pool2.impl.GenericObjectPoolConfig;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.annots.Experimental;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.csc.Cache;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.Pool;

public class ConnectionPool extends Pool<Connection> {
   public ConnectionPool(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      this(new ConnectionFactory(hostAndPort, clientConfig));
   }

   @Experimental
   public ConnectionPool(HostAndPort hostAndPort, JedisClientConfig clientConfig, Cache clientSideCache) {
      this(new ConnectionFactory(hostAndPort, clientConfig, clientSideCache));
   }

   public ConnectionPool(PooledObjectFactory<Connection> factory) {
      super(factory);
   }

   public ConnectionPool(HostAndPort hostAndPort, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig) {
      this(new ConnectionFactory(hostAndPort, clientConfig), poolConfig);
   }

   @Experimental
   public ConnectionPool(HostAndPort hostAndPort, JedisClientConfig clientConfig, Cache clientSideCache, GenericObjectPoolConfig<Connection> poolConfig) {
      this(new ConnectionFactory(hostAndPort, clientConfig, clientSideCache), poolConfig);
   }

   public ConnectionPool(PooledObjectFactory<Connection> factory, GenericObjectPoolConfig<Connection> poolConfig) {
      super(factory, poolConfig);
   }

   public Connection getResource() {
      Connection conn = (Connection)super.getResource();
      conn.setHandlingPool(this);
      return conn;
   }
}
