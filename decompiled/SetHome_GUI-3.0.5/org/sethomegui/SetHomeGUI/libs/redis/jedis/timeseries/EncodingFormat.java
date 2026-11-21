package org.sethomegui.SetHomeGUI.libs.redis.jedis.timeseries;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

public enum EncodingFormat implements Rawable {
   COMPRESSED,
   UNCOMPRESSED;

   private final byte[] raw = SafeEncoder.encode(this.name());

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
