package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.SkillTargetMarks;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.TargetDebuffBonus;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HuntersMarkSkill extends SkillBonus implements ProcSkill, TargetDebuffBonus {
   public static final String SKILL_ID = "bows_hunters_mark";
   private static final double BASE_PROC_CHANCE = 0.25;
   private static final long MARK_DURATION = 10000L;
   private static final double BASE_BONUS_DAMAGE = 0.11;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final SkillTargetMarks marks = new SkillTargetMarks();

   public HuntersMarkSkill() {
      super(SkillType.BOWS, 25, "Hunter's Mark", "Mark enemies to take increased damage from you.", SkillBonusType.PROC, 2, "bows_hunters_mark");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.25, 0.0025, 0.5, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         if (event.getEliteMobEntity().getLivingEntity() != null) {
            LivingEntity target = event.getEliteMobEntity().getLivingEntity();
            marks.mark(player.getUniqueId(), target.getUniqueId(), 10000L);
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));
         }
      }
   }

   public static boolean isMarkedBy(LivingEntity target, Player player) {
      return marks.isMarkedBy(target.getUniqueId(), player.getUniqueId());
   }

   public double getMarkBonus(int skillLevel) {
      return scaled(0.11, 0.002, skillLevel);
   }

   @Override
   public boolean appliesTo(LivingEntity target, Player attacker) {
      return isMarkedBy(target, attacker);
   }

   @Override
   public SkillType levelSource() {
      return SkillType.BOWS;
   }

   @Override
   public double bonusFor(Player attacker, LivingEntity target, int level) {
      return this.getMarkBonus(level);
   }

   @Override
   public String debugLabel() {
      return "HuntersMark=";
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
            "markBonus",
            String.format("%.1f", this.getMarkBonus(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getMarkBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("markBonus", String.format("%.1f", this.getMarkBonus(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      marks.clear();
   }
}
