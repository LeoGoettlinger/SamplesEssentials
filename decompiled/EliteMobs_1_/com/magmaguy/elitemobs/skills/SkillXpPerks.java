package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.config.SkillsConfig;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.permissions.Permissible;

final class SkillXpPerks {
   private static final String[] PERKS = new String[]{
      "customboost", "quadruple", "triple", "150percentboost", "double", "50percentboost", "25percentboost", "10percentboost"
   };
   private static final int[] PERCENTAGES = new int[]{0, 400, 300, 250, 200, 150, 125, 110};
   private static final Map<SkillType, String[]> NODES = new EnumMap<>(SkillType.class);
   private static final BigDecimal MAX_XP = BigDecimal.valueOf(Long.MAX_VALUE);

   private SkillXpPerks() {
   }

   static long apply(Permissible player, SkillType skill, long xp) {
      if (xp <= 0L) {
         return 0L;
      } else {
         String[] nodes = NODES.get(skill);

         for (int i = 0; i < nodes.length; i++) {
            if (player.hasPermission(nodes[i])) {
               if (i == 0) {
                  double multiplier = SkillsConfig.getCustomXpPerkMultiplier();
                  if (multiplier == 1.0) {
                     return xp;
                  }

                  return BigDecimal.valueOf(xp).multiply(BigDecimal.valueOf(multiplier)).min(MAX_XP).longValue();
               }

               int percent = PERCENTAGES[i];
               long whole = xp / 100L;
               long fraction = xp % 100L * percent / 100L;
               if (whole > (Long.MAX_VALUE - fraction) / percent) {
                  return Long.MAX_VALUE;
               }

               return whole * percent + fraction;
            }
         }

         return xp;
      }
   }

   static {
      for (SkillType skill : SkillType.values()) {
         String[] nodes = new String[PERKS.length];

         for (int i = 0; i < PERKS.length; i++) {
            nodes[i] = "elitemobs.perks.xp." + PERKS[i] + "." + skill.name().toLowerCase(Locale.ROOT);
         }

         NODES.put(skill, nodes);
      }
   }
}
