package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ChillingBoltSkill extends MagicWeaponSkill implements ProcSkill {
   public static final String SKILL_ID = "wands_chilling_bolt";
   private static final int SLOW_TICKS = 40;

   public ChillingBoltSkill() {
      super(SkillType.WANDS, 1, "Chilling Bolt", "Missiles can chill enemies, slowing them.", SkillBonusType.PROC, "wands_chilling_bolt");
   }

   @Override
   public boolean canProc(Player player, Object context) {
      return strike(context, MagicStrike.WAND_MISSILE) != null;
   }

   @Override
   public double getProcChance(int skillLevel) {
      return scaled(0.03, 4.0E-4, 0.1, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
      if (event != null && event.getEliteMobEntity().getLivingEntity() != null) {
         LivingEntity target = event.getEliteMobEntity().getLivingEntity();
         target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
         target.getWorld().spawnParticle(Particle.SNOWFLAKE, chest(target), 14, 0.3, 0.4, 0.3, 0.01);
      }
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
      return this.applyFormattedBonusTemplate(Map.of("procChance", String.format("%.1f", this.getProcChance(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }
}
