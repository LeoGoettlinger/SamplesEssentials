package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
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
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JudgmentSkill extends SkillBonus implements ProcSkill, TargetDebuffBonus {
   public static final String SKILL_ID = "maces_judgment";
   private static final double BASE_PROC_CHANCE = 0.17;
   private static final long MARK_DURATION = 10000L;
   private static final double BASE_DAMAGE_BONUS = 0.14;
   private static final SkillTargetMarks marks = new SkillTargetMarks();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public JudgmentSkill() {
      super(SkillType.MACES, 25, "Judgment", "Mark enemies for judgment, causing them to take bonus damage.", SkillBonusType.PROC, 2, "maces_judgment");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.17, 0.002, 0.4, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            LivingEntity target = eliteEntity.getLivingEntity();
            UUID entityUUID = target.getUniqueId();
            marks.mark(player.getUniqueId(), target.getUniqueId(), 10000L);
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));
            target.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0.0, 1.5, 0.0), 20, 0.5, 0.5, 0.5, 0.05);
         }
      }
   }

   public static boolean isJudged(LivingEntity target, Player player) {
      return marks.isMarkedBy(target.getUniqueId(), player.getUniqueId());
   }

   public double getJudgmentDamageBonus(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateValue(skillLevel) : scaled(0.14, 0.002, skillLevel);
   }

   @Override
   public boolean appliesTo(LivingEntity target, Player attacker) {
      return isJudged(target, attacker);
   }

   @Override
   public SkillType levelSource() {
      return SkillType.MACES;
   }

   @Override
   public double bonusFor(Player attacker, LivingEntity target, int level) {
      return this.getJudgmentDamageBonus(level);
   }

   @Override
   public String debugLabel() {
      return "Judgment=";
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      marks.clearSource(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      this.removeBonus(player);
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
            "damageBonus",
            String.format("%.0f", this.getJudgmentDamageBonus(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getJudgmentDamageBonus(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("damageBonus", String.format("%.0f", this.getJudgmentDamageBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      marks.clear();
      activePlayers.clear();
   }
}
