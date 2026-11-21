package org.sethomegui.SetHomeGUI.libs.redis.jedis;

public abstract class BinaryJedisShardedPubSub extends JedisShardedPubSubBase<byte[]> {
   protected final byte[] encode(byte[] raw) {
      return raw;
   }
}
