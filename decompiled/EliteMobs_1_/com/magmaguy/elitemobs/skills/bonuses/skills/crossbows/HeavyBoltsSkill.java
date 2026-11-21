package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HeavyBoltsSkill extends SkillBonus {
   public static final String SKILL_ID = "crossbows_heavy_bolts";
   private static final double BASE_DAMAGE_BONUS = 0.1;
   private static final double BASE_KNOCKBACK = 1.2;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public HeavyBoltsSkill() {
      super(SkillType.CROSSBOWS, 50, "Heavy Bolts", "Bolts deal bonus damage and apply knockback.", SkillBonusType.PASSIVE, 3, "crossbows_heavy_bolts");
   }

   public void applyKnockback(Player player, LivingEntity target) {
      int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.CROSSBOWS);
      double knockbackStrength = 1.2 + skillLevel * 0.01;
      Vector knockback = target.getLocation().toVector().subtract(player.getLocation().toVector());
      if (!(knockback.lengthSquared() < 0.001)) {
         knockback.normalize().multiply(knockbackStrength);
         knockback.setY(0.6);
         target.setVelocity(target.getVelocity().add(knockback));
      }
   }

   private double getDamageBonus(int skillLevel) {
      return scaled(0.1, 0.002, skillLevel);
   }

   private double getKnockbackStrength(int skillLevel) {
      return scaled(1.2, 0.01, skillLevel);
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
      return this.applyLoreTemplates(
         Map.of(
            "damage", String.format("%.1f", this.getDamageBonus(skillLevel) * 100.0), "knockback", String.format("%.1f", this.getKnockbackStrength(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("damage", String.format("%.1f", this.getDamageBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
