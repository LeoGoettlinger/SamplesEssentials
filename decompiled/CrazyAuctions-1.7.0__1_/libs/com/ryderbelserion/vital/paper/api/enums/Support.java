package libs.com.ryderbelserion.vital.paper.api.enums;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public enum Support {
   oraxen("Oraxen"),
   items_adder("ItemsAdder"),
   head_database("HeadDatabase"),
   cmi("CMI"),
   fancy_holograms("FancyHolograms"),
   decent_holograms("DecentHolograms"),
   factions_uuid("FactionsUUID"),
   vault("Vault"),
   yard_watch("YardWatch"),
   world_guard("WorldGuard"),
   mcmmo("McMMO"),
   placeholder_api("PlaceholderAPI"),
   luckperms("LuckPerms");

   private final String name;

   private Support(@NotNull final String name) {
      this.name = name;
   }

   public final boolean isEnabled() {
      return Bukkit.getServer().getPluginManager().isPluginEnabled(this.name);
   }

   @NotNull
   public final String getName() {
      return this.name;
   }
}
