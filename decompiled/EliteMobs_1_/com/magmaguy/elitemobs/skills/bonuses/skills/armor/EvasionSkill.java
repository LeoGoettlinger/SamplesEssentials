package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

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
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class EvasionSkill extends SkillBonus implements ProcSkill {
   private static final double EVASION_CHANCE = 0.2;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public EvasionSkill() {
      super(SkillType.ARMOR, 10, "Evasion", "Chance to completely dodge incoming attacks", SkillBonusType.PROC, 1, "armor_evasion");
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
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
      return 1.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("procChance", String.format("%.1f", this.getProcChance(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : 0.2;
   }

   @Override
   public void onProc(Player player, Object context) {
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.3, 0.5, 0.3, 0.05);
      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.5F, 1.5F);
   }

   public boolean tryEvade(Player player, Object context) {
      int skillLevel = this.getPlayerSkillLevel(player);
      if (ThreadLocalRandom.current().nextDouble() < this.getProcChance(skillLevel)) {
         this.onProc(player, context);
         return true;
      } else {
         return false;
      }
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
   }
}
