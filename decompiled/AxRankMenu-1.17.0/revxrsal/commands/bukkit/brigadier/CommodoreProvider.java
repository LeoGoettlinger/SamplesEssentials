package revxrsal.commands.bukkit.brigadier;

import java.util.Objects;
import java.util.function.Function;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.Version;

public final class CommodoreProvider {
   private static final Function<BukkitCommandHandler, Commodore> PROVIDER = checkSupported();

   private CommodoreProvider() {
      throw new AssertionError();
   }

   private static Function<BukkitCommandHandler, Commodore> checkSupported() {
      if (BukkitVersion.isPaper()) {
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_4)) {
            return plugin -> new PaperLifecycleEvents(plugin);
         }

         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_19)) {
            return plugin -> new PaperCommodore(plugin);
         }
      }

      return Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_19) ? null : plugin -> new ReflectionCommodore(plugin);
   }

   public static boolean isSupported() {
      return PROVIDER != null;
   }

   public static Commodore getCommodore(BukkitCommandHandler plugin) {
      Objects.requireNonNull(plugin, "plugin");
      return PROVIDER == null ? null : PROVIDER.apply(plugin);
   }
}
