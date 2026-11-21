package dev.demora.demoralegendaryweapons.util;

import org.bukkit.Bukkit;

public final class MinecraftVersion {
   private final int major;
   private final int minor;
   private final int patch;
   private final String raw;

   private MinecraftVersion(int major, int minor, int patch, String raw) {
      this.major = major;
      this.minor = minor;
      this.patch = patch;
      this.raw = raw;
   }

   public static MinecraftVersion detect() {
      String bukkitVersion = Bukkit.getBukkitVersion();
      String version = bukkitVersion.split("-")[0];
      String[] parts = version.split("\\.");
      int major = parsePart(parts, 0, 1);
      int minor = parsePart(parts, 1, 0);
      int patch = parsePart(parts, 2, 0);
      return new MinecraftVersion(major, minor, patch, version);
   }

   private static int parsePart(String[] parts, int index, int fallback) {
      if (index >= parts.length) {
         return fallback;
      } else {
         try {
            return Integer.parseInt(parts[index].trim());
         } catch (NumberFormatException var4) {
            return fallback;
         }
      }
   }

   public int getMajor() {
      return this.major;
   }

   public int getMinor() {
      return this.minor;
   }

   public int getPatch() {
      return this.patch;
   }

   public boolean isAtLeast(int minorVersion) {
      return this.isAtLeast(minorVersion, 0);
   }

   public boolean isAtLeast(int minorVersion, int patchVersion) {
      if (this.major != 1) {
         return this.major > 1;
      } else {
         return this.minor != minorVersion ? this.minor > minorVersion : this.patch >= patchVersion;
      }
   }

   public boolean isExactly(int minorVersion) {
      return this.major == 1 && this.minor == minorVersion;
   }

   public boolean isOlderThan(int minorVersion) {
      return !this.isAtLeast(minorVersion);
   }

   @Override
   public String toString() {
      return this.raw;
   }
}
