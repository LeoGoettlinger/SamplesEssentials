package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import java.util.Set;
import java.util.regex.Pattern;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.commands.ProtocolCommand;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.ScanParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.ScanResult;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.Hashing;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.JedisClusterHashTag;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.KeyValue;

@Deprecated
public class ShardedCommandObjects extends CommandObjects {
   private final Hashing algo;
   private final Pattern tagPattern;
   private static final String KEYS_PATTERN_MESSAGE = "Cluster mode only supports KEYS command with pattern containing hash-tag ( curly-brackets enclosed string )";
   private static final String SCAN_PATTERN_MESSAGE = "Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )";

   public ShardedCommandObjects(Hashing algo) {
      this(algo, null);
   }

   public ShardedCommandObjects(Hashing algo, Pattern tagPattern) {
      this.algo = algo;
      this.tagPattern = tagPattern;
   }

   protected ShardedCommandArguments commandArguments(ProtocolCommand command) {
      ShardedCommandArguments comArgs = new ShardedCommandArguments(this.algo, this.tagPattern, command);
      if (this.keyPreProcessor != null) {
         comArgs.setKeyArgumentPreProcessor(this.keyPreProcessor);
      }

      return comArgs;
   }

   @Override
   public CommandObject<Long> dbSize() {
      throw new UnsupportedOperationException();
   }

   @Override
   public final CommandObject<Set<String>> keys(String pattern) {
      if (!JedisClusterHashTag.isClusterCompliantMatchPattern(pattern)) {
         throw new IllegalArgumentException("Cluster mode only supports KEYS command with pattern containing hash-tag ( curly-brackets enclosed string )");
      } else {
         return new CommandObject<>(this.commandArguments(Protocol.Command.KEYS).key(pattern).processKey(pattern), BuilderFactory.STRING_SET);
      }
   }

   @Override
   public final CommandObject<Set<byte[]>> keys(byte[] pattern) {
      if (!JedisClusterHashTag.isClusterCompliantMatchPattern(pattern)) {
         throw new IllegalArgumentException("Cluster mode only supports KEYS command with pattern containing hash-tag ( curly-brackets enclosed string )");
      } else {
         return new CommandObject<>(this.commandArguments(Protocol.Command.KEYS).key(pattern).processKey(pattern), BuilderFactory.BINARY_SET);
      }
   }

   @Override
   public final CommandObject<ScanResult<String>> scan(String cursor) {
      throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
   }

   @Override
   public final CommandObject<ScanResult<String>> scan(String cursor, ScanParams params) {
      String match = params.match();
      if (match != null && JedisClusterHashTag.isClusterCompliantMatchPattern(match)) {
         return new CommandObject<>(this.commandArguments(Protocol.Command.SCAN).add(cursor).addParams(params).processKey(match), BuilderFactory.SCAN_RESPONSE);
      } else {
         throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
      }
   }

   @Override
   public final CommandObject<ScanResult<String>> scan(String cursor, ScanParams params, String type) {
      String match = params.match();
      if (match != null && JedisClusterHashTag.isClusterCompliantMatchPattern(match)) {
         return new CommandObject<>(
            this.commandArguments(Protocol.Command.SCAN).add(cursor).addParams(params).processKey(match).add((Rawable)Protocol.Keyword.TYPE).add(type),
            BuilderFactory.SCAN_RESPONSE
         );
      } else {
         throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
      }
   }

   @Override
   public final CommandObject<ScanResult<byte[]>> scan(byte[] cursor) {
      throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
   }

   @Override
   public final CommandObject<ScanResult<byte[]>> scan(byte[] cursor, ScanParams params) {
      byte[] match = params.binaryMatch();
      if (match != null && JedisClusterHashTag.isClusterCompliantMatchPattern(match)) {
         return new CommandObject<>(
            this.commandArguments(Protocol.Command.SCAN).add(cursor).addParams(params).processKey(match), BuilderFactory.SCAN_BINARY_RESPONSE
         );
      } else {
         throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
      }
   }

   @Override
   public final CommandObject<ScanResult<byte[]>> scan(byte[] cursor, ScanParams params, byte[] type) {
      byte[] match = params.binaryMatch();
      if (match != null && JedisClusterHashTag.isClusterCompliantMatchPattern(match)) {
         return new CommandObject<>(
            this.commandArguments(Protocol.Command.SCAN).add(cursor).addParams(params).processKey(match).add((Rawable)Protocol.Keyword.TYPE).add(type),
            BuilderFactory.SCAN_BINARY_RESPONSE
         );
      } else {
         throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
      }
   }

   @Override
   public final CommandObject<Long> waitReplicas(int replicas, long timeout) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CommandObject<KeyValue<Long, Long>> waitAOF(long numLocal, long numReplicas, long timeout) {
      throw new UnsupportedOperationException();
   }
}
