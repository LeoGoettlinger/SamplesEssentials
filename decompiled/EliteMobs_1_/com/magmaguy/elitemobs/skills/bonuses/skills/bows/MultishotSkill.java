package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.testing.CombatSimulator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.util.Vector;

public class MultishotSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "bows_multishot";
   private static final double BASE_PROC_CHANCE = 0.2;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public MultishotSkill() {
      super(SkillType.BOWS, 25, "Multishot", "Chance to fire additional arrows when attacking.", SkillBonusType.PROC, 2, "bows_multishot");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.2, 0.002, 0.4, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (!CombatSimulator.isTestingActive(player)) {
         if (context instanceof EliteMobDamagedByPlayerEvent event) {
            if (event.getEntityDamageByEntityEvent() != null && event.getEntityDamageByEntityEvent().getDamager() instanceof Projectile origin) {
               if (!ItemTagger.isSecondaryArrow(origin)) {
                  ItemTagger.ArrowCombatSnapshot attack = ItemTagger.snapshotArrowCombat(origin);
                  int skillLevel = event.getRangedSkillLevel();
                  Vector direction = player.getLocation().getDirection();
                  double spread = 0.15;
                  int extraArrows = 2 + skillLevel / 40;

                  for (int i = 0; i < extraArrows; i++) {
                     Vector offset = new Vector(
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * spread,
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * spread,
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * spread
                     );
                     Arrow arrow = (Arrow)player.launchProjectile(Arrow.class, direction.clone().add(offset).normalize().multiply(2));
                     attack.applyToSecondary(arrow, 0.5);
                     arrow.setPickupStatus(PickupStatus.DISALLOWED);
                  }
               }
            }
         }
      }
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
      int extraArrows = 2 + skillLevel / 40;
      return this.applyLoreTemplates(
         Map.of("procChance", String.format("%.1f", this.getProcChance(skillLevel) * 100.0), "extraArrows", String.valueOf(extraArrows))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getProcChance(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("procChance", String.format("%.0f", this.getProcChance(skillLevel) * 100.0)));
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
