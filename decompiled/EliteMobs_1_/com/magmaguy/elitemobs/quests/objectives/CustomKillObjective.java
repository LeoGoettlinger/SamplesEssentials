package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import java.util.List;
import lombok.Generated;

public class CustomKillObjective extends KillObjective {
   private final String customBossFilename;

   public CustomKillObjective(String customBossFilename, int targetKillAmount, int questLevel) {
      super(targetKillAmount, objectiveName(customBossFilename));
      this.customBossFilename = customBossFilename;
   }

   @Override
   public String getObjectiveName() {
      return objectiveName(this.customBossFilename);
   }

   private static String objectiveName(String filename) {
      CustomBossesConfigFields boss = CustomBossesConfig.getCustomBoss(filename);
      if (boss == null) {
         return filename;
      } else {
         String name = boss.getName();

         for (String token : List.of("$normalLevel", "$minibossLevel", "$bossLevel", "$reinforcementLevel", "$eventBossLevel", "$level")) {
            name = name.replace(token, "");
         }

         return ChatColorConverter.convert(name).strip();
      }
   }

   @Override
   public void checkProgress(EliteMobDeathEvent event, QuestObjectives questObjectives) {
      if (event.getEliteEntity() instanceof CustomBossEntity customBossEntity) {
         String var5 = customBossEntity.getCustomBossesConfigFields().getFilename();
         if (customBossEntity.getPhaseBossEntity() != null) {
            var5 = customBossEntity.getPhaseBossEntity().getPhase1Config().getFilename();
         }

         if (var5.equals(this.customBossFilename)) {
            this.progressObjective(questObjectives);
         }
      }
   }

   @Generated
   public String getCustomBossFilename() {
      return this.customBossFilename;
   }
}
