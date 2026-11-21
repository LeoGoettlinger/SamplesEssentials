package org.evenmorefish.fish.libs.daisylib.version;

import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public enum MinecraftVersion {
   V1_21_1("1.21.1", 121, 1),
   V1_21_3("1.21.3", 121, 3),
   V1_21_4("1.21.4", 121, 4),
   V1_21_5("1.21.5", 121, 5),
   V1_21_6("1.21.6", 121, 6),
   V1_21_7("1.21.7", 121, 7),
   V1_21_8("1.21.8", 121, 8),
   V1_21_9("1.21.9", 121, 9),
   V1_21_10("1.21.10", 121, 10),
   V1_21_11("1.21.11", 121, 11),
   V26_1("26.1", 261, 0),
   V26_1_1("26.1.1", 261, 1),
   V26_1_2("26.1.2", 261, 2),
   V26_2("26.2", 262, 2),
   V26_3("26.3", 263, 0);

   private static final MinecraftVersion CURRENT = getVersionOrThrow(Bukkit.getMinecraftVersion());
   private final String versionStr;
   private final int major;
   private final int minor;

   private MinecraftVersion(@NonNull String versionStr, int major, int minor) {
      this.versionStr = versionStr;
      this.major = major;
      this.minor = minor;
   }

   public boolean isNewerThan(@NonNull MinecraftVersion version) {
      return this.major > version.major ? true : this.major == version.major && this.minor > version.minor;
   }

   public boolean isNewerThanOrEqualTo(@NonNull MinecraftVersion version) {
      return this.major > version.major ? true : this.major == version.major && this.minor >= version.minor;
   }

   public boolean isOlderThan(@NonNull MinecraftVersion version) {
      return this.major < version.major ? true : this.major == version.major && this.minor < version.minor;
   }

   public boolean isOlderThanOrEqualTo(@NonNull MinecraftVersion version) {
      return this.major < version.major ? true : this.major == version.major && this.minor <= version.minor;
   }

   public boolean isEqualTo(@NonNull MinecraftVersion version) {
      return this == version;
   }

   @NonNull
   public static MinecraftVersion getCurrentVersion() {
      return CURRENT;
   }

   @Nullable
   public static MinecraftVersion getVersion(@NonNull String versionStr) {
      for (MinecraftVersion version : values()) {
         if (version.versionStr.equals(versionStr)) {
            return version;
         }
      }

      return null;
   }

   @NonNull
   public static MinecraftVersion getVersionOrThrow(@NonNull String versionStr) {
      MinecraftVersion version = getVersion(versionStr);
      if (version == null) {
         throw new RuntimeException(versionStr + " does not match any MinecraftVersion.");
      } else {
         return version;
      }
   }
}
