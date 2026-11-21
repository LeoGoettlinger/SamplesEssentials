package org.sethomegui.SetHomeGUI.libs.redis.jedis.gears;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.commands.ProtocolCommand;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

@Deprecated
public class RedisGearsProtocol {
   @Deprecated
   public static enum GearsCommand implements ProtocolCommand {
      @Deprecated
      TFUNCTION,
      @Deprecated
      TFCALL,
      @Deprecated
      TFCALLASYNC;

      private final byte[] raw = SafeEncoder.encode(this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   @Deprecated
   public static enum GearsKeyword implements Rawable {
      CONFIG,
      REPLACE,
      LOAD,
      DELETE,
      LIST,
      WITHCODE,
      LIBRARY,
      VERBOSE;

      private final byte[] raw = SafeEncoder.encode(this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }
}
