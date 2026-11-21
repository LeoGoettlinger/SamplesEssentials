package me.byteful.plugin.leveltools.libs.lamp.bukkit.brigadier;

import java.util.Objects;
import java.util.function.Function;
import me.byteful.plugin.leveltools.libs.lamp.bukkit.BukkitCommandHandler;

public final class CommodoreProvider {
   private static final Function<BukkitCommandHandler, Commodore> PROVIDER = checkSupported();

   private CommodoreProvider() {
      throw new AssertionError();
   }

   private static Function<BukkitCommandHandler, Commodore> checkSupported() {
      if (!BukkitVersion.isBrigadierSupported()) {
         return null;
      } else {
         if (BukkitVersion.isPaper()) {
            if (BukkitVersion.supports(1, 20, 6)) {
               return plugin -> new PaperLifecycleEvents(plugin);
            }

            if (BukkitVersion.supports(1, 19)) {
               return plugin -> new PaperCommodore(plugin);
            }
         }

         return plugin -> new ReflectionCommodore(plugin);
      }
   }

   public static boolean isSupported() {
      return PROVIDER != null;
   }

   public static Commodore getCommodore(BukkitCommandHandler plugin) {
      Objects.requireNonNull(plugin, "plugin");
      return PROVIDER == null ? null : PROVIDER.apply(plugin);
   }
}
