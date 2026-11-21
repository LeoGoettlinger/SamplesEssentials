package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.SkillTargetMarks;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.TargetDebuffBonus;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class HexBrandSkill extends MagicWeaponSkill implements ProcSkill, TargetDebuffBonus {
   public static final String SKILL_ID = "wands_hex_brand";
   private static final long BRAND_MILLIS = 6000L;
   private final SkillTargetMarks brands = new SkillTargetMarks();

   public HexBrandSkill() {
      super(SkillType.WANDS, 1, "Hex Brand", "Missiles can brand enemies to take more of your damage.", SkillBonusType.PROC, "wands_hex_brand");
   }

   @Override
   public boolean canProc(Player player, Object context) {
      return strike(context, MagicStrike.WAND_MISSILE) != null;
   }

   @Override
   public double getProcChance(int skillLevel) {
      return scaled(0.15, 0.002, 0.45, skillLevel);
   }

   private double brandBonus(int skillLevel) {
      return scaled(0.11, 0.002, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
      if (event != null && event.getEliteMobEntity().getLivingEntity() != null) {
         LivingEntity target = event.getEliteMobEntity().getLivingEntity();
         this.brands.mark(player.getUniqueId(), target.getUniqueId(), 6000L);
         target.getWorld().spawnParticle(Particle.WITCH, chest(target), 12, 0.3, 0.4, 0.3, 0.0);
      }
   }

   @Override
   public boolean appliesTo(LivingEntity target, Player attacker) {
      return this.brands.isMarkedBy(target.getUniqueId(), attacker.getUniqueId());
   }

   @Override
   public SkillType levelSource() {
      return SkillType.WANDS;
   }

   @Override
   public double bonusFor(Player attacker, LivingEntity target, int level) {
      return this.brandBonus(level);
   }

   @Override
   public String debugLabel() {
      return "HexBrand=";
   }

   @Override
   protected void clearPlayer(UUID playerId) {
      this.brands.clearSource(playerId);
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "brandBonus",
            String.format("%.1f", this.brandBonus(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.brandBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("brandBonus", String.format("%.1f", this.brandBonus(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      this.brands.clear();
      super.shutdown();
   }
}
