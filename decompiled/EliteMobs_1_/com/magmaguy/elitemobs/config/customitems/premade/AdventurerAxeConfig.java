package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerAxeConfig extends AdventurerRewardItemConfig {
   public AdventurerAxeConfig() {
      super("Axe", Material.DIAMOND_AXE, List.of("SHARPNESS,3", "UNBREAKING,2", "FIRE_ASPECT,1"));
   }
}
