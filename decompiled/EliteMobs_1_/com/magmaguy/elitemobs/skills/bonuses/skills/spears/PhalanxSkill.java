package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PhalanxSkill extends SkillBonus {
   public static final String SKILL_ID = "spears_phalanx";
   private static final double BASE_DAMAGE_REDUCTION = 0.2;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public PhalanxSkill() {
      super(SkillType.SPEARS, 25, "Phalanx", "Reduced damage from frontal attacks.", SkillBonusType.PASSIVE, 2, "spears_phalanx");
   }

   public double getDamageMultiplier(Player player, LivingEntity attacker) {
      if (!this.isActive(player)) {
         return 1.0;
      } else if (!this.isFrontalAttack(player, attacker)) {
         return 1.0;
      } else {
         int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
         double reduction = this.getDamageReduction(skillLevel);
         return 1.0 - reduction;
      }
   }

   private boolean isFrontalAttack(Player player, LivingEntity attacker) {
      Location playerLoc = player.getLocation();
      Location attackerLoc = attacker.getLocation();
      Vector playerDirection = playerLoc.getDirection().setY(0).normalize();
      Vector toAttacker = attackerLoc.toVector().subtract(playerLoc.toVector()).setY(0).normalize();
      double dot = playerDirection.dot(toAttacker);
      return dot > 0.0;
   }

   public double getDamageReduction(int skillLevel) {
      if (this.configFields != null) {
         return clampDefensiveReduction(this.configFields.calculateValue(skillLevel));
      } else {
         double reduction = Math.min(0.35, 0.2 + skillLevel * 0.002);
         return clampDefensiveReduction(reduction);
      }
   }

   public static double applyFrontalReduction(Player player, Object event, double currentDamage) {
      if (!activePlayers.contains(player.getUniqueId())) {
         return currentDamage;
      } else if (WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand()) != SkillType.SPEARS) {
         return currentDamage;
      } else {
         SkillBonus skill = SkillBonusRegistry.getSkillById("spears_phalanx");
         if (skill instanceof PhalanxSkill phalanx) {
            if (event instanceof PlayerDamagedByEliteMobEvent pde) {
               LivingEntity attacker = pde.getAttacker();
               if (attacker == null) {
                  return currentDamage;
               } else {
                  double multiplier = phalanx.getDamageMultiplier(player, attacker);
                  if (multiplier != 1.0) {
                     SkillBonus.sendSkillActionBar(player, skill);
                     skill.incrementProcCount(player);
                  }

                  return currentDamage * multiplier;
               }
            } else {
               return currentDamage;
            }
         } else {
            return currentDamage;
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
      return this.applyLoreTemplates(Map.of("value", String.format("%.0f", this.getDamageReduction(skillLevel) * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageReduction(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("value", String.format("%.0f", this.getDamageReduction(skillLevel) * 100.0)));
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
