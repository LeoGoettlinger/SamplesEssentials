package org.sethomegui.SetHomeGUI.libs.redis.jedis.util;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;

public class LazyRawable implements Rawable {
   private byte[] raw = null;

   public void setRaw(byte[] raw) {
      this.raw = raw;
   }

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
