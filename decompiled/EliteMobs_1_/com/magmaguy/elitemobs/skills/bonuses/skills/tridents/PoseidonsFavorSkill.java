package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
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

public class PoseidonsFavorSkill extends SkillBonus {
   public static final String SKILL_ID = "tridents_poseidons_favor";
   private static final int BUFF_DURATION = 200;
   private static final double BASE_DAMAGE_BONUS = 0.1;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public PoseidonsFavorSkill() {
      super(
         SkillType.TRIDENTS,
         25,
         "Poseidon's Favor",
         "On hit, grants water breathing, dolphin's grace, and conduit power. Also provides passive damage bonus.",
         SkillBonusType.PASSIVE,
         2,
         "tridents_poseidons_favor"
      );
   }

   public void onHit(Player player, EliteMobDamagedByPlayerEvent event) {
      if (this.isActive(player)) {
         int skillLevel = this.getPlayerSkillLevel(player);
         int amplifier = this.calculateAmplifier(skillLevel);
         player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 200, 0));
         player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, amplifier));
         player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 200, 0));
      }
   }

   private int calculateAmplifier(int skillLevel) {
      return this.configFields != null ? (int)Math.floor(this.configFields.calculateValue(skillLevel)) : Math.min(2, skillLevel / 25);
   }

   private double calculateDamageBonus(int skillLevel) {
      return this.configFields != null ? 0.1 * this.configFields.calculateValue(skillLevel) : scaled(0.1, 0.002, skillLevel);
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.TRIDENTS);
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
      int amplifier = this.calculateAmplifier(skillLevel);
      double damageBonus = this.calculateDamageBonus(skillLevel) * 100.0;
      return this.applyLoreTemplates(Map.of("dolphinLevel", String.valueOf(amplifier + 1), "damageBonus", String.format("%.1f", damageBonus)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.calculateDamageBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("damageBonus", String.format("%.1f", this.calculateDamageBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
