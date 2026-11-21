package com.github.retrooper.packetevents.util;

import java.time.Instant;

public final class PEVersions {
   public static final String RAW = "2.13.0";
   public static final Instant BUILD_TIMESTAMP = Instant.ofEpochMilli(1782121127652L);
   public static final PEVersion CURRENT = new PEVersion(2, 13, 0, null);
   public static final PEVersion UNKNOWN = new PEVersion(0, 0, 0);

   private PEVersions() {
      throw new IllegalStateException();
   }
}
