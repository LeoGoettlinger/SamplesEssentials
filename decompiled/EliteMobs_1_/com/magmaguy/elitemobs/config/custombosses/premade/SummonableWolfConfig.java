package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import java.util.Collections;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class SummonableWolfConfig extends CustomBossesConfigFields {
   public SummonableWolfConfig() {
      super("summonable_wolf", EntityType.WOLF, true, "$normalLevel Wolfy", "dynamic");
      this.setTimeout(3);
      this.setHealthMultiplier(3.0);
      this.setDropsVanillaLoot(false);
      this.setDropsEliteMobsLoot(false);
      this.setTrails(Collections.singletonList(Material.BONE.toString()));
      this.setOnDamagedMessages(Collections.singletonList("Woof!"));
      this.setOnDamageMessages(Collections.singletonList("Woof!"));
   }
}
