package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.List;
import org.bukkit.Material;

public class GrapplingArrowConfig extends CustomItemsConfigFields {
   public GrapplingArrowConfig() {
      super("grappling_arrow", true, Material.SPECTRAL_ARROW, "&6Grappling Arrow", List.of("&eFire at a TARGET BLOCK to grapple!"));
      this.setEnchantments(List.of("elitemobs:grappling_hook,1"));
      this.setScalability(CustomItem.Scalability.FIXED);
      this.setDropWeight("1");
   }
}
