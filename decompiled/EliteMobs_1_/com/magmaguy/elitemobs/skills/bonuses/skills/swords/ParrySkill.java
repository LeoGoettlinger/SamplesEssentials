package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ParrySkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "swords_parry";
   private static final double BASE_DAMAGE_REDUCTION = 0.3;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public ParrySkill() {
      super(SkillType.SWORDS, 50, "Parry", "Block attacks with your sword to reduce damage further.", SkillBonusType.CONDITIONAL, 3, "swords_parry");
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      if (!(context instanceof PlayerDamagedByEliteMobEvent)) {
         return false;
      } else if (!player.isBlocking()) {
         return false;
      } else {
         Material mainHand = player.getInventory().getItemInMainHand().getType();
         return mainHand.name().endsWith("_SWORD");
      }
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return scaled(0.3, 0.005, 0.6, skillLevel);
   }

   public static double applyParryReduction(Player player, PlayerDamagedByEliteMobEvent event, double currentDamage) {
      if (!activePlayers.contains(player.getUniqueId())) {
         return currentDamage;
      } else if (!event.isPlayerBlocking()) {
         return currentDamage;
      } else {
         Material mainHand = player.getInventory().getItemInMainHand().getType();
         if (!mainHand.name().endsWith("_SWORD")) {
            return currentDamage;
         } else {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
            double reduction = Math.min(0.6, 0.3 + skillLevel * 0.005);
            double reducedDamage = currentDamage * (1.0 - reduction);
            if (reducedDamage != currentDamage) {
               SkillBonus parrySkill = SkillBonusRegistry.getSkillById("swords_parry");
               if (parrySkill != null) {
                  parrySkill.incrementProcCount(player);
               }
            }

            return reducedDamage;
         }
      }
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      double reduction = this.getConditionalBonus(skillLevel) * 100.0;
      return this.applyLoreTemplates(Map.of("value", String.format("%.1f", reduction)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
