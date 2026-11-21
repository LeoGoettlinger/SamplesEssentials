package org.sethomegui.SetHomeGUI.libs.redis.jedis;

public abstract class BinaryJedisPubSub extends JedisPubSubBase<byte[]> {
   protected final byte[] encode(byte[] raw) {
      return raw;
   }
}
