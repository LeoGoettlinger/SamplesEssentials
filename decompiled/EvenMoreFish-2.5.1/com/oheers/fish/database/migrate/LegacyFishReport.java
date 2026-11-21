package com.oheers.fish.database.migrate;

import java.time.Instant;

public class LegacyFishReport {
   private final String r;
   private final String n;
   private final int c;
   private final long t;
   private final float l;

   public LegacyFishReport(String r, String n, float l, int c, long t) {
      this.r = r;
      this.n = n;
      this.c = c;
      this.l = l;
      this.t = this.calcTimeEpoch(t);
   }

   private long calcTimeEpoch(long timeEpoch) {
      return timeEpoch == -1L ? Instant.now().getEpochSecond() : timeEpoch;
   }

   public long getTimeEpoch() {
      return this.t;
   }

   public String getRarity() {
      return this.r;
   }

   public String getName() {
      return this.n;
   }

   public int getNumCaught() {
      return this.c;
   }

   public float getLargestLength() {
      return this.l;
   }

   @Override
   public String toString() {
      return "LegacyFishReport{rarity='" + this.r + "', name='" + this.n + "', numCaught=" + this.c + ", time=" + this.t + ", length=" + this.l + "}";
   }
}
