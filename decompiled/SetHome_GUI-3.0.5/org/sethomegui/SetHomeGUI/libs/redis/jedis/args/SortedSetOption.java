package org.sethomegui.SetHomeGUI.libs.redis.jedis.args;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

public enum SortedSetOption implements Rawable {
   MIN,
   MAX;

   private final byte[] raw = SafeEncoder.encode(this.name());

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
