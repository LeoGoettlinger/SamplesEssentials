package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CrushingBlowSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "maces_crushing_blow";
   private static final double BASE_PROC_CHANCE = 0.12;
   private static final double BASE_DAMAGE_MULTIPLIER = 1.34;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public CrushingBlowSkill() {
      super(SkillType.MACES, 10, "Crushing Blow", "Chance to land a crushing hit that deals bonus damage.", SkillBonusType.PROC, 1, "maces_crushing_blow");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.12, 0.003, 0.3, skillLevel);
   }

   public double getDamageMultiplier(int skillLevel) {
      return scaled(1.34, 0.008, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         if (event.getEliteMobEntity().getLivingEntity() != null) {
            event.getEliteMobEntity()
               .getLivingEntity()
               .getWorld()
               .spawnParticle(
                  Particle.BLOCK,
                  event.getEliteMobEntity().getLivingEntity().getLocation().add(0.0, 1.0, 0.0),
                  20,
                  0.3,
                  0.3,
                  0.3,
                  0.1,
                  Material.IRON_BLOCK.createBlockData()
               );
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1.5F);
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
      return this.applyLoreTemplates(
         Map.of(
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "multiplier",
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
            "multiplier",
            String.format("%.1f", this.getDamageMultiplier(skillLevel)),
            "procChance",
            String.format("%.0f", this.getProcChance(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
