package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EchoBoltSkill extends MagicWeaponSkill implements ProcSkill {
   public static final String SKILL_ID = "wands_echo_bolt";
   private static final long ECHO_DELAY_TICKS = 10L;
   private final Set<BukkitRunnable> echoes = ConcurrentHashMap.newKeySet();

   public EchoBoltSkill() {
      super(SkillType.WANDS, 3, "Echo Bolt", "Missiles can echo and strike again.", SkillBonusType.PROC, "wands_echo_bolt");
   }

   @Override
   public boolean canProc(Player player, Object context) {
      return strike(context, MagicStrike.WAND_MISSILE) != null;
   }

   @Override
   public double getProcChance(int skillLevel) {
      return scaled(0.1, 0.002, 0.4, skillLevel);
   }

   @Override
   public void onProc(final Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
      if (event != null && event.getEliteMobEntity().getLivingEntity() != null) {
         final LivingEntity target = event.getEliteMobEntity().getLivingEntity();
         final double damage = event.getDamage();
         BukkitRunnable echo = new BukkitRunnable() {
            public void run() {
               EchoBoltSkill.this.echoes.remove(this);
               if (target.isValid() && !target.isDead()) {
                  target.getWorld().spawnParticle(Particle.END_ROD, EchoBoltSkill.chest(target), 10, 0.2, 0.3, 0.2, 0.05);
                  EchoBoltSkill.sideDamage(player, target, damage);
               }
            }
         };
         this.echoes.add(echo);
         echo.runTaskLater(MetadataHandler.PLUGIN, 10L);
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

   @Override
   public void shutdown() {
      this.echoes.forEach(BukkitRunnable::cancel);
      this.echoes.clear();
      super.shutdown();
   }
}
