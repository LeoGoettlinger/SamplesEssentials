package org.sethomegui.SetHomeGUI.libs.redis.jedis.timeseries;

public class TSElement {
   private final long timestamp;
   private final double value;

   public TSElement(long timestamp, double value) {
      this.timestamp = timestamp;
      this.value = value;
   }

   public long getTimestamp() {
      return this.timestamp;
   }

   public double getValue() {
      return this.value;
   }

   @Override
   public int hashCode() {
      return 31 * Long.hashCode(this.timestamp) + Long.hashCode(Double.doubleToLongBits(this.value));
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else if (!(obj instanceof TSElement)) {
         return false;
      } else {
         TSElement other = (TSElement)obj;
         return this.timestamp == other.timestamp && this.value == other.value;
      }
   }

   @Override
   public String toString() {
      return "(" + this.timestamp + ":" + this.value + ")";
   }
}
