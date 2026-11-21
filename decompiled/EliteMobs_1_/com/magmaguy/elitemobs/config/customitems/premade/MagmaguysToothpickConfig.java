package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import java.util.List;
import org.bukkit.Material;

public class MagmaguysToothpickConfig extends CustomItemsConfigFields {
   public MagmaguysToothpickConfig() {
      super(
         "magmaguys_toothpick",
         true,
         Material.WOODEN_SWORD,
         "<g:#FF4500:#FF6347>Magmaguy</g>&c's <g:#228B22:#32CD32>Toothpick</g>",
         List.of("&aIn nearly mint condition!")
      );
      this.setEnchantments(List.of("VANISHING_CURSE,1"));
      this.setCustomModelID("elitemobs:gear/magmaguys_toothpick");
   }
}
