package org.sethomegui.SetHomeGUI.libs.redis.jedis.timeseries;

import java.util.Locale;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

public enum AggregationType implements Rawable {
   AVG,
   SUM,
   MIN,
   MAX,
   RANGE,
   COUNT,
   FIRST,
   LAST,
   STD_P("STD.P"),
   STD_S("STD.S"),
   VAR_P("VAR.P"),
   VAR_S("VAR.S"),
   TWA;

   private final byte[] raw;

   private AggregationType() {
      this.raw = SafeEncoder.encode(this.name());
   }

   private AggregationType(String alt) {
      this.raw = SafeEncoder.encode(alt);
   }

   @Override
   public byte[] getRaw() {
      return this.raw;
   }

   public static AggregationType safeValueOf(String str) {
      try {
         return valueOf(str.replace('.', '_').toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }
}
