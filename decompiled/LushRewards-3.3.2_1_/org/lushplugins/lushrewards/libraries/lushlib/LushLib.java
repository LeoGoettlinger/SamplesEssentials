package org.lushplugins.lushrewards.libraries.lushlib;

import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.plugin.SpigotPlugin;

public final class LushLib {
   private static LushLib instance;
   private boolean enabled = false;
   private SpigotPlugin plugin = null;

   public void enable(@NotNull SpigotPlugin plugin) {
      this.enabled = true;
      this.plugin = plugin;
      LushLogger.setLogger(plugin.getLogger());
      LushLogger.getLogger().info("Successfully enabled LushLib");
   }

   public void disable() {
      LushLogger.getLogger().info("Successfully disabled LushLib");
      this.plugin = null;
      this.enabled = false;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public SpigotPlugin getPlugin() {
      return this.plugin;
   }

   public static LushLib getInstance() {
      if (instance == null) {
         instance = new LushLib();
      }

      return instance;
   }
}
