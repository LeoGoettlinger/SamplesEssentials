package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import java.util.List;
import java.util.regex.Pattern;
import org.sethomegui.SetHomeGUI.libs.pool2.impl.GenericObjectPoolConfig;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.providers.ShardedConnectionProvider;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.Hashing;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.IOUtils;

@Deprecated
public class ShardedPipeline extends MultiNodePipelineBase {
   private final ShardedConnectionProvider provider;
   private AutoCloseable closeable = null;

   public ShardedPipeline(List<HostAndPort> shards, JedisClientConfig clientConfig) {
      this(new ShardedConnectionProvider(shards, clientConfig));
      this.closeable = this.provider;
   }

   public ShardedPipeline(ShardedConnectionProvider provider) {
      super(new ShardedCommandObjects(provider.getHashingAlgo()));
      this.provider = provider;
   }

   public ShardedPipeline(
      List<HostAndPort> shards, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig, Hashing algo, Pattern tagPattern
   ) {
      this(new ShardedConnectionProvider(shards, clientConfig, poolConfig, algo), tagPattern);
      this.closeable = this.provider;
   }

   public ShardedPipeline(ShardedConnectionProvider provider, Pattern tagPattern) {
      super(new ShardedCommandObjects(provider.getHashingAlgo(), tagPattern));
      this.provider = provider;
   }

   @Override
   public void close() {
      try {
         super.close();
      } finally {
         IOUtils.closeQuietly(this.closeable);
      }
   }

   @Override
   protected HostAndPort getNodeKey(CommandArguments args) {
      return this.provider.getNode(((ShardedCommandArguments)args).getKeyHash());
   }

   @Override
   protected Connection getConnection(HostAndPort nodeKey) {
      return this.provider.getConnection(nodeKey);
   }

   public void prepareGraphCommands() {
      super.prepareGraphCommands(this.provider);
   }
}
