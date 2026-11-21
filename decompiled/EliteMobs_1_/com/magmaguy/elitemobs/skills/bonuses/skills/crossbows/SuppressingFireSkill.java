package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

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
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SuppressingFireSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "crossbows_suppressing_fire";
   private static final double BASE_PROC_CHANCE = 0.2;
   private static final int DEBUFF_DURATION = 80;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public SuppressingFireSkill() {
      super(SkillType.CROSSBOWS, 50, "Suppressing Fire", "Chance to weaken and slow enemies on hit.", SkillBonusType.PROC, 3, "crossbows_suppressing_fire");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.2, 0.002, 0.4, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         if (event.getEliteMobEntity().getLivingEntity() != null) {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.CROSSBOWS);
            int amplifier = Math.min(2, skillLevel / 30);
            event.getEliteMobEntity().getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, amplifier));
            event.getEliteMobEntity().getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
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
      return this.applyLoreTemplates(Map.of("procChance", String.format("%.1f", this.getProcChance(skillLevel) * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getProcChance(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("procChance", String.format("%.0f", this.getProcChance(skillLevel) * 100.0)));
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
