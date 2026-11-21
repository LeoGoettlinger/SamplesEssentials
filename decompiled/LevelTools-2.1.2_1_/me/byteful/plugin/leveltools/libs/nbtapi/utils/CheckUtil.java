package me.byteful.plugin.leveltools.libs.nbtapi.utils;

import me.byteful.plugin.leveltools.libs.nbtapi.NbtApiException;

public class CheckUtil {
   private CheckUtil() {
   }

   public static void assertAvailable(MinecraftVersion version) {
      if (!MinecraftVersion.isAtLeastVersion(version)) {
         throw new NbtApiException("This Method is only avaliable for the version " + version.name() + " and above!");
      }
   }
}
