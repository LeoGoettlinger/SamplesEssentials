package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerLeggingsConfig extends AdventurerRewardItemConfig {
   public AdventurerLeggingsConfig() {
      super("Leggings", Material.DIAMOND_LEGGINGS, List.of("SHARPNESS,4", "PROTECTION,2", "UNBREAKING,1"));
   }
}
