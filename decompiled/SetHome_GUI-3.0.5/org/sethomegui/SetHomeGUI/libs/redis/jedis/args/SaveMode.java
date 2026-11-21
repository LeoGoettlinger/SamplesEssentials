package org.sethomegui.SetHomeGUI.libs.redis.jedis.args;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

public enum SaveMode implements Rawable {
   NOSAVE,
   SAVE;

   private final byte[] raw = SafeEncoder.encode(this.name());

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
