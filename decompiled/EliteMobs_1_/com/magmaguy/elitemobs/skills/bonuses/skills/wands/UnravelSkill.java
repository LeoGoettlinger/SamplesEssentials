package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.TargetDebuffBonus;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class UnravelSkill extends MagicWeaponSkill implements ProcSkill, TargetDebuffBonus {
   public static final String SKILL_ID = "wands_unravel";
   private static final long DURATION_MILLIS = 5000L;
   private final Map<UUID, UnravelSkill.Unravelled> unravelled = new ConcurrentHashMap<>();

   public UnravelSkill() {
      super(SkillType.WANDS, 3, "Unravel", "Missiles can unravel an enemy's defenses for everyone.", SkillBonusType.PROC, "wands_unravel");
   }

   @Override
   public boolean canProc(Player player, Object context) {
      return strike(context, MagicStrike.WAND_MISSILE) != null;
   }

   @Override
   public double getProcChance(int skillLevel) {
      return scaled(0.1, 0.002, 0.4, skillLevel);
   }

   private double unravelBonus(int skillLevel) {
      return scaled(0.1, 0.001, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
      if (event != null && event.getEliteMobEntity().getLivingEntity() != null) {
         LivingEntity target = event.getEliteMobEntity().getLivingEntity();
         long now = System.currentTimeMillis();
         this.unravelled.values().removeIf(state -> state.expiresAt() <= now);
         this.unravelled.put(target.getUniqueId(), new UnravelSkill.Unravelled(this.unravelBonus(this.skillLevel(player)), now + 5000L));
         target.getWorld().spawnParticle(Particle.ENCHANT, chest(target), 20, 0.4, 0.5, 0.4, 0.5);
      }
   }

   @Override
   public boolean appliesTo(LivingEntity target, Player attacker) {
      UnravelSkill.Unravelled state = this.unravelled.get(target.getUniqueId());
      return state != null && state.expiresAt() > System.currentTimeMillis();
   }

   @Override
   public SkillType levelSource() {
      return SkillType.WANDS;
   }

   @Override
   public double bonusFor(Player attacker, LivingEntity target, int level) {
      UnravelSkill.Unravelled state = this.unravelled.get(target.getUniqueId());
      return state == null ? 0.0 : state.bonus();
   }

   @Override
   public String debugLabel() {
      return "Unravel=";
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "procChance", String.format("%.1f", this.getProcChance(skillLevel) * 100.0), "bonus", String.format("%.1f", this.unravelBonus(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.unravelBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("bonus", String.format("%.1f", this.unravelBonus(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      this.unravelled.clear();
      super.shutdown();
   }

   private record Unravelled(double bonus, long expiresAt) {
   }
}
