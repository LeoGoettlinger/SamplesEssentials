package com.magmaguy.elitemobs.skills;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

final class WeaponXpAttribution {
   private WeaponXpAttribution() {
   }

   static Map<SkillType, Long> distribute(long earnedXp, double totalPlayerDamage, Map<SkillType, Double> contributions) {
      if (earnedXp > 0L && Double.isFinite(totalPlayerDamage) && !(totalPlayerDamage <= 0.0) && contributions != null && !contributions.isEmpty()) {
         EnumMap<SkillType, Long> shares = new EnumMap<>(SkillType.class);

         for (Entry<SkillType, Double> entry : contributions.entrySet()) {
            SkillType skill = entry.getKey();
            Double attributed = entry.getValue();
            if (skill != null && skill.isWeaponSkill() && attributed != null && Double.isFinite(attributed) && !(attributed <= 0.0)) {
               long share = (long)Math.floor(earnedXp * Math.min(1.0, attributed / totalPlayerDamage));
               if (share > 0L) {
                  shares.put(skill, share);
               }
            }
         }

         return Map.copyOf(shares);
      } else {
         return Map.of();
      }
   }
}
