package xyz.lychee.lagfixer.utils;

import lombok.Generated;

public class TimingUtil {
   private long startTime;
   private long endTime;

   public static TimingUtil startNew() {
      return new TimingUtil().start();
   }

   public TimingUtil start() {
      this.startTime = System.nanoTime();
      return this;
   }

   public TimingUtil stop() {
      this.endTime = System.nanoTime();
      return this;
   }

   public long getExecutingTime() {
      return this.getExecutingNanoTime() / 1000000L;
   }

   public long getExecutingNanoTime() {
      return this.startTime != 0L && this.endTime != 0L ? this.endTime - this.startTime : 0L;
   }

   @Override
   public String toString() {
      return MathUtils.round(this.getExecutingNanoTime() / 1000000.0, 3) + "ms";
   }

   @Generated
   public long getStartTime() {
      return this.startTime;
   }

   @Generated
   public long getEndTime() {
      return this.endTime;
   }
}
