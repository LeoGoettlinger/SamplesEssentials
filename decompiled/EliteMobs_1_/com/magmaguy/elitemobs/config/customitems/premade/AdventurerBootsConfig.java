package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerBootsConfig extends AdventurerRewardItemConfig {
   public AdventurerBootsConfig() {
      super("Boots", Material.DIAMOND_BOOTS, List.of("SHARPNESS,4", "PROTECTION,2", "UNBREAKING,2", "FEATHER_FALLING,2"));
   }
}
