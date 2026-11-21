package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class ZombieDadConfig extends CustomBossesConfigFields {
   public ZombieDadConfig() {
      super("zombie_parents_dad", EntityType.SKELETON, true, "$reinforcementLevel &7Zombie Dad", "dynamic");
      this.setHealthMultiplier(0.5);
      this.setDamageMultiplier(0.5);
      this.setDropsVanillaLoot(false);
      this.setDropsEliteMobsLoot(false);
   }
}
