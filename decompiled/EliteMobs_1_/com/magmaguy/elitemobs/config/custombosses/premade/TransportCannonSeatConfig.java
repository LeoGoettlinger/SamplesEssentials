package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.EntityType;

public final class TransportCannonSeatConfig extends CustomBossesConfigFields {
   public TransportCannonSeatConfig() {
      super("transport_cannon_seat", EntityType.PIG, true, "", "1");
      this.setDropsEliteMobsLoot(false);
      this.setDropsVanillaLoot(false);
      this.setDropsRandomLoot(false);
      this.setDropsSkillXP(false);
      this.setAi(true);
   }

   @Override
   public void processConfigFields() {
      if (this.fileConfiguration.getKeys(false).isEmpty()) {
         this.fileConfiguration.set("eliteScript.HideSeat.Events", List.of("EliteMobSpawnEvent"));
         this.fileConfiguration
            .set(
               "eliteScript.HideSeat.Actions",
               List.of(
                  Map.of(
                     "action",
                     "POTION_EFFECT",
                     "potionEffectType",
                     "INVISIBILITY",
                     "amplifier",
                     0,
                     "duration",
                     Integer.MAX_VALUE,
                     "Target",
                     Map.<String, String>of("targetType", "SELF")
                  )
               )
            );
         this.fileConfiguration.setComments("entityType", List.of("An invisible seat for cannon travel. Use another customboss file for a visible mount."));
      }

      super.processConfigFields();
   }
}
