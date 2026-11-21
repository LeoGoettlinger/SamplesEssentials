package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerSwordConfig extends AdventurerRewardItemConfig {
   public AdventurerSwordConfig() {
      super("Sword", Material.DIAMOND_SWORD, List.of("SHARPNESS,4", "UNBREAKING,2", "FIRE_ASPECT,1"));
   }
}
