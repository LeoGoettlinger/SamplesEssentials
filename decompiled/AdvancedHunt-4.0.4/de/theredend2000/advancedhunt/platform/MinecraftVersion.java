package de.theredend2000.advancedhunt.platform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;

final class MinecraftVersion {
   private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?.*$");
   private final int major;
   private final int minor;
   private final int patch;

   private MinecraftVersion(int major, int minor, int patch) {
      this.major = major;
      this.minor = minor;
      this.patch = patch;
   }

   static MinecraftVersion detect() {
      String raw = Bukkit.getBukkitVersion();
      String base = raw == null ? "" : raw.split("-")[0];
      Matcher m = VERSION_PATTERN.matcher(base);
      if (!m.matches()) {
         Bukkit.getLogger().warning("[AdvancedHunt] Failed to parse Minecraft version '" + raw + "', defaulting to 1.8");
         return new MinecraftVersion(1, 8, 8);
      } else {
         int major = parseInt(m.group(1), 1);
         int minor = parseInt(m.group(2), 8);
         int patch = parseInt(m.group(3), 0);
         return new MinecraftVersion(major, minor, patch);
      }
   }

   boolean isAtMostMinor(int maxMinor) {
      return this.major == 1 && this.minor <= maxMinor;
   }

   boolean isLessThan(int otherMajor, int otherMinor, int otherPatch) {
      if (this.major != otherMajor) {
         return this.major < otherMajor;
      } else {
         return this.minor != otherMinor ? this.minor < otherMinor : this.patch < otherPatch;
      }
   }

   private static int parseInt(String s, int fallback) {
      if (s == null) {
         return fallback;
      } else {
         try {
            return Integer.parseInt(s);
         } catch (NumberFormatException var3) {
            return fallback;
         }
      }
   }

   @Override
   public String toString() {
      return this.major + "." + this.minor + "." + this.patch;
   }
}
