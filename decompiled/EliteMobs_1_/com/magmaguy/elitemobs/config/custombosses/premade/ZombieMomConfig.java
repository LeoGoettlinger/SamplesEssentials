package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class ZombieMomConfig extends CustomBossesConfigFields {
   public ZombieMomConfig() {
      super("zombie_parents_mom", EntityType.SKELETON, true, "$reinforcementLevel &7Zombie Mom", "dynamic");
      this.setHealthMultiplier(0.5);
      this.setDamageMultiplier(0.5);
      this.setDropsVanillaLoot(false);
      this.setDropsEliteMobsLoot(false);
   }
}
