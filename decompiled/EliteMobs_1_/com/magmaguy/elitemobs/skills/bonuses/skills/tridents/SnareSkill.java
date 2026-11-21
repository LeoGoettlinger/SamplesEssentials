package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

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

public class SnareSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "tridents_snare";
   private static final double BASE_PROC_CHANCE = 0.25;
   private static final int SNARE_DURATION = 60;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public SnareSkill() {
      super(SkillType.TRIDENTS, 10, "Snare", "Trident attacks have a chance to heavily slow and ground targets.", SkillBonusType.PROC, 1, "tridents_snare");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.25, 0.002, 0.45, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            LivingEntity target = eliteEntity.getLivingEntity();
            int skillLevel = this.getPlayerSkillLevel(player);
            int amplifier = this.calculateAmplifier(skillLevel);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3 + amplifier));
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 128));
         }
      }
   }

   private int calculateAmplifier(int skillLevel) {
      return this.configFields != null ? (int)Math.floor(this.configFields.calculateValue(skillLevel)) : (int)Math.floor(skillLevel * 0.05);
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
      double procChance = this.getProcChance(skillLevel) * 100.0;
      int amplifier = this.calculateAmplifier(skillLevel);
      return this.applyLoreTemplates(Map.of("procChance", String.format("%.1f", procChance), "slownessLevel", String.valueOf(4 + amplifier)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.calculateAmplifier(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "slownessLevel",
            String.valueOf(4 + this.calculateAmplifier(skillLevel)),
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0)
         )
      );
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
