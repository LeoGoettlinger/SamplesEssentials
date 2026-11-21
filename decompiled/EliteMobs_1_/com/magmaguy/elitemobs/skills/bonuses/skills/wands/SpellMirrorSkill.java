package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SpellMirrorSkill extends MagicWeaponSkill implements CooldownSkill {
   public static final String SKILL_ID = "wands_spell_mirror";
   private static final long COOLDOWN_SECONDS = 15L;
   private static final double COUNTER_MULTIPLIER = 2.0;

   public SpellMirrorSkill() {
      super(SkillType.WANDS, 4, "Spell Mirror", "Negate the next hit every 15 seconds and answer it.", SkillBonusType.COOLDOWN, "wands_spell_mirror");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return 15L;
   }

   @Override
   public boolean triggersOnOffensiveHit() {
      return false;
   }

   public static boolean tryMirror(Player player, PlayerDamagedByEliteMobEvent event, double damage) {
      if (SkillBonusRegistry.getSkillById("wands_spell_mirror") instanceof SpellMirrorSkill mirror && mirror.wieldedBy(player) && !mirror.isOnCooldown(player)) {
         mirror.startCooldown(player, mirror.skillLevel(player));
         LivingEntity attacker = event.getAttacker();
         if (attacker != null && attacker.getWorld().equals(player.getWorld())) {
            beam(player.getEyeLocation(), chest(attacker), Particle.ENCHANT);
            sideDamage(player, attacker, damage * 2.0);
         }

         player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0.0, 1.0, 0.0), 16, 0.4, 0.6, 0.4, 0.02);
         player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 1.2F);
         mirror.incrementProcCount(player);
         SkillBonus.sendSkillActionBar(player, mirror);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("counterPercent", String.format("%.0f", 200.0), "cooldown", String.valueOf(15L)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return 2.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("cooldown", String.valueOf(15L)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }
}
