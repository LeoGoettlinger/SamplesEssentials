package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class BattleHardenedSkill extends SkillBonus {
   private static final double DAMAGE_REDUCTION = 0.2;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public BattleHardenedSkill() {
      super(SkillType.ARMOR, 10, "Battle Hardened", "Passive damage reduction from all sources", SkillBonusType.PASSIVE, 1, "armor_battle_hardened");
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      this.onDeactivate(player);
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
      return this.applyLoreTemplates(Map.of("value", String.format("%.1f", this.getBonusValue(skillLevel) * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return clampDefensiveReduction(this.configFields == null ? 0.2 : this.configFields.calculateValue(skillLevel));
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", this.getBonusValue(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
