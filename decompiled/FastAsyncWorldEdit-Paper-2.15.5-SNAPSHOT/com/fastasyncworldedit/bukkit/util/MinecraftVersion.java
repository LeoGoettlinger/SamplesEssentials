package com.fastasyncworldedit.bukkit.util;

import com.google.common.collect.ComparisonChain;
import com.sk89q.worldedit.bukkit.paperlib.PaperLib;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;

public class MinecraftVersion implements Comparable<MinecraftVersion> {
   public static final MinecraftVersion NETHER = new MinecraftVersion(1, 16);
   public static final MinecraftVersion CAVES_17 = new MinecraftVersion(1, 17);
   public static final MinecraftVersion CAVES_18 = new MinecraftVersion(1, 18);
   private static MinecraftVersion current = null;
   private final int major;
   private final int minor;
   private final int release;

   public MinecraftVersion(int major, int minor, int release) {
      this.major = major;
      this.minor = minor;
      this.release = release;
   }

   public MinecraftVersion(int major, int minor) {
      this(major, minor, 0);
   }

   @Deprecated(
      since = "2.4.10",
      forRemoval = true
   )
   public MinecraftVersion() {
      MinecraftVersion current = getCurrent();
      this.major = current.getMajor();
      this.minor = current.getMinor();
      this.release = current.getRelease();
   }

   public static MinecraftVersion getCurrent() {
      return current == null ? (current = detectMinecraftVersion()) : current;
   }

   private static MinecraftVersion detectMinecraftVersion() {
      int major;
      int minor;
      int release;
      if (PaperLib.isPaper()) {
         String minecraftVersion = Bukkit.getMinecraftVersion().split(" ")[0];
         String[] parts = minecraftVersion.split(Pattern.quote("."));
         if (parts.length != 2 && parts.length != 3) {
            throw new IllegalStateException("Failed to determine minecraft version!");
         }

         major = Integer.parseInt(parts[0]);
         minor = Integer.parseInt(parts[1]);
         release = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
      } else {
         String packageVersion = getPackageVersion();
         String[] parts = packageVersion.split("_");
         if (parts.length == 3 && parts[0].startsWith("v") && parts[2].startsWith("R")) {
            major = Integer.parseInt(parts[0].substring(1));
            minor = Integer.parseInt(parts[1]);
            release = Integer.parseInt(parts[2].substring(1));
         } else {
            String version = Bukkit.getServer().getBukkitVersion().split("-")[0];
            String[] dotted = version.split(Pattern.quote("."));
            if (dotted.length != 2 && dotted.length != 3) {
               throw new IllegalStateException("Failed to determine minecraft version!");
            }

            major = Integer.parseInt(dotted[0]);
            minor = Integer.parseInt(dotted[1]);
            release = dotted.length == 3 ? Integer.parseInt(dotted[2]) : 0;
         }
      }

      return new MinecraftVersion(major, minor, release);
   }

   private static String getPackageVersion() {
      String fullPackagePath = Bukkit.getServer().getClass().getPackage().getName();
      if (fullPackagePath.contains(".")) {
         String token = fullPackagePath.substring(fullPackagePath.lastIndexOf(46) + 1);
         if (token.startsWith("v") && token.contains("_R")) {
            return token;
         }
      }

      String version = Bukkit.getServer().getBukkitVersion();
      return version.split("-")[0];
   }

   public boolean isEqual(MinecraftVersion other) {
      return this.compareTo(other) == 0;
   }

   public boolean isEqualOrHigherThan(MinecraftVersion other) {
      return this.compareTo(other) >= 0;
   }

   public boolean isEqualOrLowerThan(MinecraftVersion other) {
      return this.compareTo(other) <= 0;
   }

   public boolean isHigherThan(MinecraftVersion other) {
      return this.compareTo(other) > 0;
   }

   public boolean isLowerThan(MinecraftVersion other) {
      return this.compareTo(other) < 0;
   }

   public int getMajor() {
      return this.major;
   }

   public int getMinor() {
      return this.minor;
   }

   public int getRelease() {
      return this.release;
   }

   public int compareTo(@Nonnull MinecraftVersion other) {
      return other.equals(this)
         ? 0
         : ComparisonChain.start()
            .compare(this.getMajor(), other.getMajor())
            .compare(this.getMinor(), other.getMinor())
            .compare(this.getRelease(), other.getRelease())
            .result();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MinecraftVersion that = (MinecraftVersion)o;
         if (this.getMajor() != that.getMajor()) {
            return false;
         } else {
            return this.getMinor() != that.getMinor() ? false : this.getRelease() == that.getRelease();
         }
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      return this.major + "." + this.minor + "." + this.release;
   }
}
