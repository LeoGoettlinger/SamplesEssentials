package com.fastasyncworldedit.core;

import com.fastasyncworldedit.core.util.StringMan;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import java.util.Locale;
import org.apache.logging.log4j.Logger;

public class FaweVersion {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   public final int year;
   public final int month;
   public final int day;
   public final int hash;
   public final int build;
   public final int[] semver;
   public final boolean snapshot;

   public FaweVersion(int year, int month, int day, int[] semver, boolean snapshot, int hash, int build) {
      this.year = year;
      this.month = month;
      this.day = day;
      this.hash = hash;
      this.build = build;
      this.semver = semver;
      this.snapshot = snapshot;
   }

   public FaweVersion(String version, String commit, String date) {
      String[] split = version.substring(version.indexOf(61) + 1).split("-");
      String[] split1 = split[0].split("\\.");
      int[] ver = new int[3];

      for (int i = 0; i < 3; i++) {
         ver[i] = Integer.parseInt(split1[i]);
      }

      this.semver = ver;
      this.snapshot = split.length > 1 && split[1].toLowerCase(Locale.ROOT).contains("snapshot");
      int buildIndex = this.snapshot ? 2 : 1;
      this.build = split.length == buildIndex + 1 ? Integer.parseInt(split[buildIndex]) : 0;
      this.hash = Integer.parseInt(commit.substring(commit.indexOf(61) + 1), 16);
      String[] split2 = date.substring(date.indexOf(61) + 1).split("\\.");
      this.year = Integer.parseInt(split2[0]);
      this.month = Integer.parseInt(split2[1]);
      this.day = Integer.parseInt(split2[2]);
   }

   public static FaweVersion tryParse(String version, String commit, String date) {
      try {
         return new FaweVersion(version, commit, date);
      } catch (Exception var4) {
         LOGGER.error("Failed to parse FaweVersion", var4);
         return new FaweVersion(0, 0, 0, null, true, 0, 0);
      }
   }

   @Override
   public String toString() {
      if (this.semver == null) {
         return "FastAsyncWorldEdit-NoVer-SNAPSHOT";
      } else {
         String snapshot = this.snapshot ? "-SNAPSHOT" : "";
         String build = this.build > 0 ? "-" + this.build : "";
         return "FastAsyncWorldEdit-" + StringMan.join(this.semver, ".") + snapshot + build;
      }
   }

   public boolean isNewer(FaweVersion other) {
      if (other.semver == null) {
         return other.build > this.build;
      } else if (this.semver == null) {
         return true;
      } else if (other.semver[0] != this.semver[0]) {
         return other.semver[0] > this.semver[0];
      } else if (other.semver[1] != this.semver[1]) {
         return other.semver[1] > this.semver[1];
      } else if (other.semver[2] != this.semver[2]) {
         return other.semver[2] > this.semver[2];
      } else {
         return other.snapshot == this.snapshot ? other.build > this.build : !other.snapshot;
      }
   }
}
