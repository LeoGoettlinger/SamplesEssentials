package org.sethomegui.SetHomeGUI.libs.redis.jedis.args;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

public enum ClientAttributeOption implements Rawable {
   LIB_NAME("LIB-NAME"),
   LIB_VER("LIB-VER");

   private final byte[] raw;

   private ClientAttributeOption(String str) {
      this.raw = SafeEncoder.encode(str);
   }

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
