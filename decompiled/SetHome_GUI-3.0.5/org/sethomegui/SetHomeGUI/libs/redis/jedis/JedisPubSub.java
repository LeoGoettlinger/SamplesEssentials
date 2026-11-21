package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

public abstract class JedisPubSub extends JedisPubSubBase<String> {
   protected final String encode(byte[] raw) {
      return SafeEncoder.encode(raw);
   }
}
