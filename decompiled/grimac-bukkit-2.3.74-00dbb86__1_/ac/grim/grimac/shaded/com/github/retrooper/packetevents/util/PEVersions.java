package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util;

import java.time.Instant;

public final class PEVersions {
   public static final String RAW = "2.13.1+8187e23-SNAPSHOT";
   public static final Instant BUILD_TIMESTAMP = Instant.ofEpochMilli(1782455710777L);
   public static final PEVersion CURRENT = new PEVersion(2, 13, 1, "8187e23");
   public static final PEVersion UNKNOWN = new PEVersion(0, 0, 0);

   private PEVersions() {
      throw new IllegalStateException();
   }
}
