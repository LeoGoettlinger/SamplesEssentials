package ac.grim.grimac.platform.bukkit;

import ac.grim.grimac.platform.api.PlatformPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import lombok.Generated;
import org.bukkit.plugin.Plugin;

public class BukkitPlatformPlugin implements PlatformPlugin {
   @NotNull
   private final Plugin plugin;

   @Override
   public boolean isEnabled() {
      return this.plugin.isEnabled();
   }

   @Override
   public String getName() {
      return this.plugin.getName();
   }

   @Override
   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   @Generated
   public BukkitPlatformPlugin(@NotNull Plugin plugin) {
      if (plugin == null) {
         throw new NullPointerException("plugin is marked non-null but is null");
      } else {
         this.plugin = plugin;
      }
   }
}
