package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class ImpaleSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "tridents_impale";
   private static final double BASE_PROC_CHANCE = 0.15;
   private static final double BASE_DAMAGE_MULTIPLIER = 1.29;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public ImpaleSkill() {
      super(SkillType.TRIDENTS, 10, "Impale", "Trident attacks have a chance to deal massive bonus damage.", SkillBonusType.PROC, 1, "tridents_impale");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.15, 0.0015, 0.25, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
   }

   private double calculateDamageMultiplier(int skillLevel) {
      return this.configFields != null ? 1.29 + this.configFields.calculateValue(skillLevel) : scaled(1.29, 0.012, skillLevel);
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
      double procChance = this.getProcChance(skillLevel) * 100.0;
      double multiplier = this.calculateDamageMultiplier(skillLevel);
      return this.applyLoreTemplates(Map.of("procChance", String.format("%.1f", procChance), "multiplier", String.format("%.1f", multiplier)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.calculateDamageMultiplier(skillLevel) - 1.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "multiplier",
            String.format("%.1f", this.calculateDamageMultiplier(skillLevel)),
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
