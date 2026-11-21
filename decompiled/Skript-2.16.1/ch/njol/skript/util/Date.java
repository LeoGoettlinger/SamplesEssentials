package ch.njol.skript.util;

import ch.njol.skript.SkriptConfig;
import ch.njol.yggdrasil.YggdrasilSerializable;
import java.util.TimeZone;
import org.jetbrains.annotations.Nullable;

public class Date extends java.util.Date implements YggdrasilSerializable {
   public static Date now() {
      return new Date();
   }

   public static Date fromJavaDate(java.util.Date date) {
      return date instanceof Date ours ? ours : new Date(date.getTime());
   }

   public Date() {
   }

   public Date(long timestamp) {
      super(timestamp);
   }

   public Date(long timestamp, TimeZone zone) {
      super(timestamp - zone.getOffset(timestamp));
   }

   public void add(Timespan other) {
      this.setTime(this.getTime() + other.getAs(Timespan.TimePeriod.MILLISECOND));
   }

   public void subtract(Timespan other) {
      this.setTime(this.getTime() - other.getAs(Timespan.TimePeriod.MILLISECOND));
   }

   public Timespan difference(Date other) {
      return new Timespan(Math.abs(this.getTime() - other.getTime()));
   }

   public Date plus(Timespan other) {
      return new Date(this.getTime() + other.getAs(Timespan.TimePeriod.MILLISECOND));
   }

   public Date minus(Timespan other) {
      return new Date(this.getTime() - other.getAs(Timespan.TimePeriod.MILLISECOND));
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public long getTimestamp() {
      return this.getTime();
   }

   @Override
   public int hashCode() {
      return 31 + Long.hashCode(this.getTime());
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj == null) {
         return false;
      } else if (obj instanceof java.util.Date other) {
         return this == obj ? true : this.getTime() == other.getTime();
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      return SkriptConfig.formatDate(this.getTime());
   }
}
