package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class DamageTestDummyConfig extends CustomBossesConfigFields {
   public DamageTestDummyConfig() {
      super("damage_test_dummy", EntityType.HUSK, true, "&8[&6Lv.50 &7Damage Test Dummy&8]", "50");
      this.setDamageMultiplier(1.0);
      this.setHealthMultiplier(1000.0);
      this.setDropsEliteMobsLoot(false);
      this.setDropsVanillaLoot(false);
      this.setDropsRandomLoot(false);
      this.setDropsSkillXP(false);
      this.setSilent(true);
      this.setAi(false);
   }
}
