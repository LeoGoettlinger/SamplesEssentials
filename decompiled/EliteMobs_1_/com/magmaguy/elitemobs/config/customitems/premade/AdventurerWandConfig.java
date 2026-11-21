package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerWandConfig extends AdventurerRewardItemConfig {
   public AdventurerWandConfig() {
      super("Wand", Material.BLAZE_ROD, List.of("POWER,3", "UNBREAKING,2", "freeminecraftmodels:multicast,2"));
      this.setWeaponType(SkillType.WANDS);
      this.setFmmItemModel("fmm_default_arcane_wand");
   }
}
