package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import java.util.Set;
import org.sethomegui.SetHomeGUI.libs.pool2.impl.GenericObjectPoolConfig;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.annots.Experimental;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.csc.Cache;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.csc.CacheConfig;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.csc.CacheFactory;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.providers.SentineledConnectionProvider;

public class JedisSentineled extends UnifiedJedis {
   public JedisSentineled(String masterName, JedisClientConfig masterClientConfig, Set<HostAndPort> sentinels, JedisClientConfig sentinelClientConfig) {
      super(new SentineledConnectionProvider(masterName, masterClientConfig, sentinels, sentinelClientConfig), masterClientConfig.getRedisProtocol());
   }

   @Experimental
   public JedisSentineled(
      String masterName, JedisClientConfig masterClientConfig, CacheConfig cacheConfig, Set<HostAndPort> sentinels, JedisClientConfig sentinelClientConfig
   ) {
      this(masterName, masterClientConfig, CacheFactory.getCache(cacheConfig), sentinels, sentinelClientConfig);
   }

   @Experimental
   public JedisSentineled(
      String masterName, JedisClientConfig masterClientConfig, Cache clientSideCache, Set<HostAndPort> sentinels, JedisClientConfig sentinelClientConfig
   ) {
      super(
         new SentineledConnectionProvider(masterName, masterClientConfig, clientSideCache, sentinels, sentinelClientConfig),
         masterClientConfig.getRedisProtocol(),
         clientSideCache
      );
   }

   public JedisSentineled(
      String masterName,
      JedisClientConfig masterClientConfig,
      GenericObjectPoolConfig<Connection> poolConfig,
      Set<HostAndPort> sentinels,
      JedisClientConfig sentinelClientConfig
   ) {
      super(
         new SentineledConnectionProvider(masterName, masterClientConfig, poolConfig, sentinels, sentinelClientConfig), masterClientConfig.getRedisProtocol()
      );
   }

   @Experimental
   public JedisSentineled(
      String masterName,
      JedisClientConfig masterClientConfig,
      Cache clientSideCache,
      GenericObjectPoolConfig<Connection> poolConfig,
      Set<HostAndPort> sentinels,
      JedisClientConfig sentinelClientConfig
   ) {
      super(
         new SentineledConnectionProvider(masterName, masterClientConfig, clientSideCache, poolConfig, sentinels, sentinelClientConfig),
         masterClientConfig.getRedisProtocol(),
         clientSideCache
      );
   }

   public JedisSentineled(SentineledConnectionProvider sentineledConnectionProvider) {
      super(sentineledConnectionProvider);
   }

   public HostAndPort getCurrentMaster() {
      return ((SentineledConnectionProvider)this.provider).getCurrentMaster();
   }

   public Pipeline pipelined() {
      return (Pipeline)super.pipelined();
   }
}
