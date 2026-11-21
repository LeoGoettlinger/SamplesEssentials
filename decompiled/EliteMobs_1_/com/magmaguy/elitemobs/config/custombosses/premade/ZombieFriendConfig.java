package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class ZombieFriendConfig extends CustomBossesConfigFields {
   public ZombieFriendConfig() {
      super("zombie_friends_friend", EntityType.ZOMBIE, true, "$reinforcementLevel &7Zombie Friend", "dynamic");
      this.setHealthMultiplier(0.5);
      this.setDamageMultiplier(0.5);
      this.setDropsVanillaLoot(false);
      this.setDropsEliteMobsLoot(false);
   }
}
