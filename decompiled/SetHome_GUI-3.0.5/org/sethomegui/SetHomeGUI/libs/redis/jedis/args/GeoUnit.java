package org.sethomegui.SetHomeGUI.libs.redis.jedis.args;

import java.util.Locale;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

public enum GeoUnit implements Rawable {
   M,
   KM,
   MI,
   FT;

   private final byte[] raw = SafeEncoder.encode(this.name().toLowerCase(Locale.ENGLISH));

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
