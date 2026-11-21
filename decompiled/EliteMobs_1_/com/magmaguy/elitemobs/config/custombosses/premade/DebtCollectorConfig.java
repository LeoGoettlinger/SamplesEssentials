package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

public class DebtCollectorConfig extends CustomBossesConfigFields {
   public DebtCollectorConfig() {
      super("debt_collector", EntityType.VINDICATOR, true, "$bossLevel <g:#8B0000:#DC143C>The Debt Collector</g>", "dynamic");
      this.setScaledCombat(true);
      this.setHealthMultiplier(5.0);
      this.setDamageMultiplier(1.0);
      this.setPersistent(false);
      this.setTimeout(600);
      this.setPowers(new ArrayList<>(List.of("attack_push.yml", "attack_blinding.lua", "movement_speed.yml", "attack_gravity.lua", "spirit_walk.yml")));
      this.setTrails(new ArrayList<>(List.of(Particle.LARGE_SMOKE.toString(), Particle.SOUL.toString(), Particle.LARGE_SMOKE.toString())));
      this.setDropsEliteMobsLoot(false);
      this.setDropsVanillaLoot(false);
      this.setDropsRandomLoot(false);
      this.setDropsSkillXP(false);
      this.setOnDamageMessages(
         new ArrayList<>(
            List.of(
               "&c[Debt Collector] &7Consider that... &einterest&7.",
               "&c[Debt Collector] &7Every hit is a &6payment&7.",
               "&c[Debt Collector] &7You can't escape your debts!"
            )
         )
      );
      this.setOnDamagedMessages(
         new ArrayList<>(
            List.of(
               "&c[Debt Collector] &7That all you got?",
               "&c[Debt Collector] &7You'll pay for that... literally.",
               "&c[Debt Collector] &7Fighting back won't clear your debt!"
            )
         )
      );
   }
}
