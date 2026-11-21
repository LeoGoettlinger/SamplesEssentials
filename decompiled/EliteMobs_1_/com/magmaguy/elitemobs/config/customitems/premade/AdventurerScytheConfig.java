package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerScytheConfig extends AdventurerRewardItemConfig {
   public AdventurerScytheConfig() {
      super("Scythe", Material.DIAMOND_HOE, List.of("SHARPNESS,3", "UNBREAKING,3", "FIRE_ASPECT,1"));
   }
}
