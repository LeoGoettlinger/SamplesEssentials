package com.magmaguy.elitemobs.advancedcombat.challenges;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import java.util.List;

public record ClassTrialDefinition(ClassFormDefinition form, CustomBossesConfigFields boss) {
   public static ClassTrialDefinition forForm(String formId) {
      ClassFormDefinition form = BuiltInClassContent.catalog().require(formId);
      List<? extends CustomBossesConfigFields> matches = CustomBossesConfig.getCustomBosses()
         .values()
         .stream()
         .filter(bossx -> formId.equals(bossx.getFileConfiguration().getString("classTrial.class")))
         .limit(2L)
         .toList();
      if (matches.size() != 1) {
         throw new IllegalArgumentException("Expected one enabled instructor for " + formId + ", found " + matches.size());
      } else {
         CustomBossesConfigFields boss = matches.getFirst();
         if (boss.getLevel() != form.requiredFoundationSkillLevel()) {
            throw new IllegalArgumentException(
               "Trial boss level must match the class admission level " + form.requiredFoundationSkillLevel() + " in " + boss.getFilename()
            );
         } else if (boss.getPowers() != null && !boss.getPowers().isEmpty()) {
            if (boss.getCustomModel() == null
               || boss.getCustomModel().isBlank()
               || CustomModel.customModelsEnabled() && CustomModel.modelExists(boss.getCustomModel())) {
               return new ClassTrialDefinition(form, boss);
            } else {
               throw new IllegalArgumentException("Missing instructor custom model: " + boss.getCustomModel());
            }
         } else {
            throw new IllegalArgumentException("Instructor requires ordinary powers: " + boss.getFilename());
         }
      }
   }
}
