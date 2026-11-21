package org.sethomegui.SetHomeGUI.libs.redis.jedis.graph;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.commands.ProtocolCommand;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

@Deprecated
public class GraphProtocol {
   @Deprecated
   public static enum GraphCommand implements ProtocolCommand {
      QUERY,
      RO_QUERY,
      DELETE,
      LIST,
      PROFILE,
      EXPLAIN,
      SLOWLOG,
      CONFIG;

      private final byte[] raw = SafeEncoder.encode("GRAPH." + this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   @Deprecated
   public static enum GraphKeyword implements Rawable {
      CYPHER,
      TIMEOUT,
      SET,
      GET,
      __COMPACT("--COMPACT");

      private final byte[] raw;

      private GraphKeyword() {
         this.raw = SafeEncoder.encode(this.name());
      }

      private GraphKeyword(String alt) {
         this.raw = SafeEncoder.encode(alt);
      }

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }
}
