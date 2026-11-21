package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerCrossbowConfig extends AdventurerRewardItemConfig {
   public AdventurerCrossbowConfig() {
      super("Crossbow", Material.CROSSBOW, List.of("POWER,3", "UNBREAKING,2", "QUICK_CHARGE,2", "PIERCING,1"));
   }
}
