package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class StaggerSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "axes_stagger";
   private static final double BASE_PROC_CHANCE = 0.12;
   private static final int STAGGER_DURATION = 40;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public StaggerSkill() {
      super(SkillType.AXES, 25, "Stagger", "Attacks can stagger enemies, slowing and weakening them.", SkillBonusType.PROC, 2, "axes_stagger");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.12, 0.002, 0.35, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity target = event.getEliteMobEntity();
         if (target != null && target.getLivingEntity() != null) {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.AXES);
            this.applyStagger(target.getLivingEntity(), skillLevel);
         }
      }
   }

   private void applyStagger(LivingEntity entity, int skillLevel) {
      int duration = 40 + skillLevel / 10;
      int slowLevel = Math.min(2, skillLevel / 30);
      int weaknessLevel = Math.min(1, skillLevel / 50);
      entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, slowLevel));
      entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, weaknessLevel));
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
      return this.applyLoreTemplates(Map.of("procChance", String.format("%.1f", this.getProcChance(skillLevel) * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getProcChance(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("procChance", String.format("%.1f", this.getProcChance(skillLevel) * 100.0)));
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
