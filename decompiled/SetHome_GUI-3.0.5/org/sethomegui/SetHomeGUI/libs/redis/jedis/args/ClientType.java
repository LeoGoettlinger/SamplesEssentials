package org.sethomegui.SetHomeGUI.libs.redis.jedis.args;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

public enum ClientType implements Rawable {
   NORMAL,
   MASTER,
   SLAVE,
   REPLICA,
   PUBSUB;

   private final byte[] raw = SafeEncoder.encode(this.name().toLowerCase());

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
