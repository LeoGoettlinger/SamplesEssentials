package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import org.sethomegui.SetHomeGUI.libs.pool2.PooledObject;
import org.sethomegui.SetHomeGUI.libs.pool2.PooledObjectFactory;
import org.sethomegui.SetHomeGUI.libs.pool2.impl.DefaultPooledObject;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.annots.Experimental;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.csc.Cache;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.csc.CacheConnection;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.exceptions.JedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionFactory implements PooledObjectFactory<Connection> {
   private static final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);
   private final JedisSocketFactory jedisSocketFactory;
   private final JedisClientConfig clientConfig;
   private Cache clientSideCache = null;

   public ConnectionFactory(HostAndPort hostAndPort) {
      this.clientConfig = DefaultJedisClientConfig.builder().build();
      this.jedisSocketFactory = new DefaultJedisSocketFactory(hostAndPort);
   }

   public ConnectionFactory(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      this.clientConfig = clientConfig;
      this.jedisSocketFactory = new DefaultJedisSocketFactory(hostAndPort, this.clientConfig);
   }

   @Experimental
   public ConnectionFactory(HostAndPort hostAndPort, JedisClientConfig clientConfig, Cache csCache) {
      this.clientConfig = clientConfig;
      this.jedisSocketFactory = new DefaultJedisSocketFactory(hostAndPort, this.clientConfig);
      this.clientSideCache = csCache;
   }

   public ConnectionFactory(JedisSocketFactory jedisSocketFactory, JedisClientConfig clientConfig) {
      this.clientConfig = clientConfig;
      this.jedisSocketFactory = jedisSocketFactory;
   }

   @Override
   public void activateObject(PooledObject<Connection> pooledConnection) throws Exception {
   }

   @Override
   public void destroyObject(PooledObject<Connection> pooledConnection) throws Exception {
      Connection jedis = pooledConnection.getObject();
      if (jedis.isConnected()) {
         try {
            jedis.close();
         } catch (RuntimeException var4) {
            logger.debug("Error while close", var4);
         }
      }
   }

   @Override
   public PooledObject<Connection> makeObject() throws Exception {
      try {
         Connection jedis = (Connection)(this.clientSideCache == null
            ? new Connection(this.jedisSocketFactory, this.clientConfig)
            : new CacheConnection(this.jedisSocketFactory, this.clientConfig, this.clientSideCache));
         return new DefaultPooledObject<>(jedis);
      } catch (JedisException var2) {
         logger.debug("Error while makeObject", var2);
         throw var2;
      }
   }

   @Override
   public void passivateObject(PooledObject<Connection> pooledConnection) throws Exception {
   }

   @Override
   public boolean validateObject(PooledObject<Connection> pooledConnection) {
      Connection jedis = pooledConnection.getObject();

      try {
         return jedis.isConnected() && jedis.ping();
      } catch (Exception var4) {
         logger.warn("Error while validating pooled Connection object.", var4);
         return false;
      }
   }
}
