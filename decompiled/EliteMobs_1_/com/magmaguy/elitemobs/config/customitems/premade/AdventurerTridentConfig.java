package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerTridentConfig extends AdventurerRewardItemConfig {
   public AdventurerTridentConfig() {
      super("Trident", Material.TRIDENT, List.of("SHARPNESS,4", "UNBREAKING,2", "LOYALTY,1"));
   }
}
