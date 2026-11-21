package dev.cwhead.GravesX.api.addon;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.addon.GravesXAddon;
import java.util.Objects;
import org.bukkit.plugin.Plugin;

public class AddonAPI {
   private final Graves plugin;

   public AddonAPI(Graves plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
   }

   public void ensureAddonFolder(Plugin addon) {
      Objects.requireNonNull(addon, "addon");
      GravesXAddon.ensureAddonFolder(this.plugin, addon.getDescription().getName());
   }

   public int exportAddonConfigs(Plugin addon) {
      Objects.requireNonNull(addon, "addon");
      return GravesXAddon.exportAddonConfigs(this.plugin, addon.getDescription().getName(), false);
   }

   public int exportAddonConfigs(Plugin addon, boolean replaceIfExists) {
      Objects.requireNonNull(addon, "addon");
      return GravesXAddon.exportAddonConfigs(this.plugin, addon.getDescription().getName(), replaceIfExists);
   }
}
