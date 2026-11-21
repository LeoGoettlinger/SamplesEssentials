package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class SnoopyConfig extends CustomBossesConfigFields {
   public SnoopyConfig() {
      super("snoopy", EntityType.WOLF, true, "&6{&4☠&6} <rainbow>Snoopy</rainbow>", "dynamic");
      this.setTimeout(3);
      this.setHealthMultiplier(10.0);
      this.setDamageMultiplier(2.0);
      this.setPowers(new ArrayList<>(List.of("invulnerability_fire.lua")));
      this.setDropsVanillaLoot(false);
      this.setDropsEliteMobsLoot(false);
      this.setTrails(Collections.singletonList(Material.BONE.toString()));
      this.setOnDamagedMessages(Collections.singletonList("Woof!"));
      this.setOnDamageMessages(Collections.singletonList("Woof!"));
      this.setSpawnMessage("&aAn extremely rare Snoopy has been sighted!");
      this.setAnnouncementPriority(3);
   }
}
