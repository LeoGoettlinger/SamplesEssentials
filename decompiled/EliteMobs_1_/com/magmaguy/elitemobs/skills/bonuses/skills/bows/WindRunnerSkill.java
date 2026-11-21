package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WindRunnerSkill extends SkillBonus {
   public static final String SKILL_ID = "bows_wind_runner";
   private static final int SPEED_DURATION = 100;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public WindRunnerSkill() {
      super(SkillType.BOWS, 50, "Wind Runner", "Successful hits grant you a speed boost.", SkillBonusType.PASSIVE, 3, "bows_wind_runner");
   }

   public void applySpeedBoost(Player player) {
      int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.BOWS);
      int amplifier = Math.min(2, skillLevel / 30);
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, amplifier));
   }

   private int getSpeedAmplifier(int skillLevel) {
      return Math.min(2, skillLevel / 30);
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
      return this.applyLoreTemplates(Map.of("speedLevel", String.valueOf(this.getSpeedAmplifier(skillLevel) + 1)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getSpeedAmplifier(skillLevel) + 1;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("speedLevel", String.valueOf(this.getSpeedAmplifier(skillLevel) + 1)));
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
