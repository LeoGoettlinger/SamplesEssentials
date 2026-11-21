package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerBowConfig extends AdventurerRewardItemConfig {
   public AdventurerBowConfig() {
      super("Bow", Material.BOW, List.of("POWER,3", "UNBREAKING,1", "FLAME,1", "INFINITY,1"));
   }
}
