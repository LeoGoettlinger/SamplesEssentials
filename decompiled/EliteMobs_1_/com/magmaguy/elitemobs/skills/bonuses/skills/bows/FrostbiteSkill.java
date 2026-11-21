package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FrostbiteSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "bows_frostbite";
   private static final double BASE_PROC_CHANCE = 0.15;
   private static final int SLOW_DURATION = 60;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public FrostbiteSkill() {
      super(SkillType.BOWS, 10, "Frostbite", "Arrows have a chance to slow and weaken enemies.", SkillBonusType.PROC, 1, "bows_frostbite");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.15, 0.002, 0.35, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         if (event.getEliteMobEntity().getLivingEntity() != null) {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.BOWS);
            int amplifier = Math.min(2, skillLevel / 30);
            event.getEliteMobEntity().getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, amplifier + 1));
            event.getEliteMobEntity().getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, amplifier));
            event.getEliteMobEntity()
               .getLivingEntity()
               .getWorld()
               .spawnParticle(Particle.SNOWFLAKE, event.getEliteMobEntity().getLivingEntity().getLocation().add(0.0, 1.0, 0.0), 15, 0.5, 0.5, 0.5, 0.05);
         }
      }
   }

   private double getDamageBonus(int skillLevel) {
      return scaled(1.4, 0.008, skillLevel);
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
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "damageBonus",
            String.format("%.0f", (this.getDamageBonus(skillLevel) - 1.0) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageBonus(skillLevel) - 1.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "procChance",
            String.format("%.0f", this.getProcChance(skillLevel) * 100.0),
            "damageBonus",
            String.format("%.0f", (this.getDamageBonus(skillLevel) - 1.0) * 100.0)
         )
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
