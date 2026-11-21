package org.sethomegui.SetHomeGUI.libs.redis.jedis.args;

public interface Rawable {
   byte[] getRaw();

   @Override
   int hashCode();

   @Override
   boolean equals(Object var1);
}
