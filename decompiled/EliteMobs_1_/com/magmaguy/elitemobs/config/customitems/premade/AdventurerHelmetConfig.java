package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerHelmetConfig extends AdventurerRewardItemConfig {
   public AdventurerHelmetConfig() {
      super("Helmet", Material.DIAMOND_HELMET, List.of("SHARPNESS,4", "PROTECTION,2", "UNBREAKING,2"));
   }
}
