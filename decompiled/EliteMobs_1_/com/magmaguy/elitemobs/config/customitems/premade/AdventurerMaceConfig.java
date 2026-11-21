package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerMaceConfig extends AdventurerRewardItemConfig {
   public AdventurerMaceConfig() {
      super("Mace", Material.getMaterial("MACE"), List.of("SHARPNESS,3", "UNBREAKING,2", "FIRE_ASPECT,1"));
   }
}
