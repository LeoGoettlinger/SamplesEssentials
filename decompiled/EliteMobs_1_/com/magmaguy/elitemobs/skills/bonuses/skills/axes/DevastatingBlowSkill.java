package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class DevastatingBlowSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "axes_devastating_blow";
   private static final double BASE_PROC_CHANCE = 0.1;
   private static final double BASE_DAMAGE_MULTIPLIER = 1.5;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public DevastatingBlowSkill() {
      super(SkillType.AXES, 10, "Devastating Blow", "Attacks have a chance to deal massive bonus damage.", SkillBonusType.PROC, 1, "axes_devastating_blow");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.1, 0.002, 0.3, skillLevel);
   }

   @Override
   public long getInternalCooldownMillis() {
      return 1500L;
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         if (event.getEliteMobEntity().getLivingEntity() != null) {
            event.getEliteMobEntity()
               .getLivingEntity()
               .getWorld()
               .spawnParticle(Particle.EXPLOSION, event.getEliteMobEntity().getLivingEntity().getLocation(), 1);
         }
      }
   }

   private double getDamageMultiplier(int skillLevel) {
      return scaled(1.5, 0.01, skillLevel);
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
            "damageMultiplier",
            String.format("%.1f", this.getDamageMultiplier(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageMultiplier(skillLevel) - 1.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "damageMultiplier",
            String.format("%.1f", this.getDamageMultiplier(skillLevel)),
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
