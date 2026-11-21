package revxrsal.commands.bukkit.brigadier;

import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.Version;

final class BukkitVersion {
   private static final int UNVERSION_NMS = 17;
   private static final String CB_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

   private BukkitVersion() {
   }

   @NotNull
   public static Class<?> findNmsClass(@NotNull String name) {
      try {
         return Class.forName("net.minecraft.server." + name);
      } catch (Throwable var2) {
         throw var2;
      }
   }

   @NotNull
   public static Class<?> findOcbClass(@NotNull String name) {
      try {
         return Class.forName(CB_PACKAGE + '.' + name);
      } catch (Throwable var2) {
         throw var2;
      }
   }

   public static boolean isPaper() {
      return Version.isPaper();
   }

   private static boolean isClassPresent(String name) {
      try {
         Class.forName(name);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   static {
      Pattern dot = Pattern.compile(".", 16);
      String bukkitVersion = Bukkit.getServer().getBukkitVersion();
      String[] version;
      if (bukkitVersion.indexOf(45) == -1) {
         version = dot.split(bukkitVersion);
      } else {
         version = dot.split(bukkitVersion.substring(0, bukkitVersion.indexOf(45)));
      }

      if (version.length == 2) {
         version = new String[]{version[0], version[1], "0"};
      }

      String minorSlice = version[2];
      if (minorSlice.indexOf(45) != -1) {
         minorSlice = minorSlice.substring(0, minorSlice.indexOf(45));
      }
   }
}
