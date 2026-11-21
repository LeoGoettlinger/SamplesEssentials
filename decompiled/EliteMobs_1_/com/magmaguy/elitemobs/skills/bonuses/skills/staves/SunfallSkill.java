package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SunfallSkill extends MagicWeaponSkill implements CooldownSkill {
   public static final String SKILL_ID = "staves_sunfall";
   private static final long COOLDOWN_SECONDS = 20L;
   private final Map<UUID, UUID> sunfallAttack = new ConcurrentHashMap<>();

   public SunfallSkill() {
      super(SkillType.STAVES, 4, "Sunfall", "Your next fireball every 20 seconds lands with triple force.", SkillBonusType.COOLDOWN, "staves_sunfall");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return 20L;
   }

   @Override
   public boolean tryActivate(Player player, Object event) {
      EliteMobDamagedByPlayerEvent hit = strike(event, MagicStrike.STAFF_FIREBALL);
      if (hit == null) {
         return false;
      } else {
         this.sunfallAttack.put(player.getUniqueId(), hit.getMagicHit().attackId());
         LivingEntity target = hit.getEliteMobEntity().getLivingEntity();
         if (target != null) {
            target.getWorld().spawnParticle(Particle.EXPLOSION, chest(target), 2, 0.5, 0.5, 0.5, 0.0);
            target.getWorld().spawnParticle(Particle.FLAME, chest(target).add(0.0, 2.0, 0.0), 60, 1.2, 2.0, 1.2, 0.04);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0F, 0.6F);
         }

         return true;
      }
   }

   @Override
   public boolean sharesActivation(Player player, Object event) {
      EliteMobDamagedByPlayerEvent hit = strike(event, MagicStrike.STAFF_FIREBALL);
      return hit != null && hit.getMagicHit().attackId().equals(this.sunfallAttack.get(player.getUniqueId()));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return scaled(1.5, 0.01, skillLevel);
   }

   @Override
   protected void clearPlayer(UUID playerId) {
      this.sunfallAttack.remove(playerId);
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("bonus", String.format("%.0f", this.getBonusValue(skillLevel) * 100.0), "cooldown", String.valueOf(20L)));
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("bonus", String.format("%.0f", this.getBonusValue(skillLevel) * 100.0), "cooldown", String.valueOf(20L)));
   }

   @Override
   public void shutdown() {
      this.sunfallAttack.clear();
      super.shutdown();
   }
}
