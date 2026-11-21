package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class NecronomiconSkeletonConfig extends CustomBossesConfigFields {
   public NecronomiconSkeletonConfig() {
      super("necronomicon_skeleton", EntityType.SKELETON, true, "$reinforcementLevel &7Summoned Skeleton", "dynamic");
      this.setHealthMultiplier(0.3);
      this.setDamageMultiplier(0.1);
      this.setDropsVanillaLoot(false);
      this.setDropsEliteMobsLoot(false);
   }
}
