package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.AdventurerRewardItemConfig;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import org.bukkit.Material;

public final class AdventurerStaffConfig extends AdventurerRewardItemConfig {
   public AdventurerStaffConfig() {
      super(
         "Staff",
         Material.getMaterial("WOODEN_SPEAR"),
         List.of("POWER,3", "UNBREAKING,2", "freeminecraftmodels:blast_radius,2", "freeminecraftmodels:ignition,2")
      );
      this.setWeaponType(SkillType.STAVES);
      this.setFmmItemModel("fmm_default_arcane_staff");
   }
}
