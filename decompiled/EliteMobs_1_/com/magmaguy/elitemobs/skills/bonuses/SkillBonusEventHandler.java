package com.magmaguy.elitemobs.skills.bonuses;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.PlayerDataLoadedEvent;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.CombatLevelDisplay;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPBar;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.IronStanceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.SecondWindSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.OverdrawSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.SteadyAimSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.GrimReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.LongReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PolearmMasterySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.FlurrySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.PoiseSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.SwiftStrikesSkill;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class SkillBonusEventHandler implements Listener {
   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.HIGH
   )
   public void onPlayerDamagedByEliteMob(PlayerDamagedByEliteMobEvent event) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         if (!SkillsConfig.isWorldExcludedFromSkills(event.getPlayer())) {
            event.applySkillBonuses();
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onDeath(PlayerDeathEvent event) {
      if (SkillBonusRegistry.getSkillById("armor_second_wind") instanceof SecondWindSkill secondWind) {
         secondWind.cancelPending(event.getEntity());
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerDataLoaded(PlayerDataLoadedEvent event) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         Player player = event.getPlayer();
         if (player.isOnline()) {
            PlayerSkillSelection.onPlayerJoin(player);
            refreshSkillState(player);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      PlayerSkillSelection.onPlayerLeave(player);
      SkillBonusRegistry.removeAllBonuses(player);
      ArmorSkillHealthBonus.resetPlayerHealthDisplay(player);
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         refreshSkillState(event.getPlayer());
      }
   }

   private static void refreshSkillState(Player player) {
      if (SkillsConfig.isWorldExcludedFromSkills(player)) {
         SkillBonusRegistry.removeAllBonuses(player);
         ArmorSkillHealthBonus.applyHealthBonus(player);
         SkillXPBar.removeAllBars(player);
         CombatLevelDisplay.removeDisplay(player);
      } else {
         SkillBonusRegistry.applyAllBonuses(player);
         ArmorSkillHealthBonus.applyHealthBonus(player);
         CombatLevelDisplay.createDisplay(player);
      }
   }

   private static SkillType getWeaponSkillTypeFromItem(ItemStack item) {
      return WeaponIdentityResolver.progressionSkill(item);
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.HIGH
   )
   public void onEliteMobDamagedByPlayer(EliteMobDamagedByPlayerEvent event) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         if (!SkillsConfig.isWorldExcludedFromSkills(event.getPlayer())) {
            if (!event.isCustomDamage() || event.getMagicHit() != null) {
               event.applySkillBonuses();
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onItemHeldChange(PlayerItemHeldEvent event) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         Player player = event.getPlayer();
         UUID uuid = player.getUniqueId();
         if (SkillsConfig.isWorldExcludedFromSkills(player)) {
            SkillBonusRegistry.removeAllBonuses(player);
            ArmorSkillHealthBonus.applyHealthBonus(player);
         } else {
            ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
            ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
            SkillType oldType = getWeaponSkillTypeFromItem(oldItem);
            SkillType newType = getWeaponSkillTypeFromItem(newItem);
            if (oldType == SkillType.SWORDS) {
               if (SwiftStrikesSkill.hasActiveSkill(uuid)) {
                  SwiftStrikesSkill.removeSpeedBonus(player);
               }

               if (PoiseSkill.hasActiveSkill(uuid)) {
                  PoiseSkill.removeKnockbackResistance(player);
               }

               if (FlurrySkill.hasActiveSkill(uuid)) {
                  FlurrySkill.removeAttackSpeedModifier(player);
               }
            }

            if (oldType == SkillType.HOES) {
               GrimReachSkill.removeReachBonus(player);
            }

            if (oldType == SkillType.SPEARS) {
               LongReachSkill.removeReachBonus(player);
               PolearmMasterySkill.removeAttackSpeedBonus(player);
            }

            if (oldType == SkillType.BOWS) {
               OverdrawSkill.stopDrawing(uuid);
            }

            if (newType == SkillType.SWORDS) {
               if (FlurrySkill.hasActiveSkill(uuid)) {
                  int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
                  FlurrySkill.applyAttackSpeedModifier(player, FlurrySkill.getAttackSpeedBonus(player, skillLevel));
               }

               if (SwiftStrikesSkill.hasActiveSkill(uuid)) {
                  int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
                  SwiftStrikesSkill.applySpeedBonus(player, skillLevel);
               }

               if (PoiseSkill.hasActiveSkill(uuid)) {
                  int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
                  PoiseSkill.applyKnockbackResistance(player, skillLevel);
               }
            }

            if (newType == SkillType.HOES) {
               int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.HOES);
               GrimReachSkill.applyReachBonus(player, skillLevel);
               SkillBonus grimReach = SkillBonusRegistry.getSkillById("hoes_grim_reach");
               if (grimReach != null && grimReach.isActive(player)) {
                  SkillBonus.sendSkillActionBar(player, grimReach);
               }
            }

            if (newType == SkillType.SPEARS) {
               int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
               LongReachSkill.applyReachBonus(player, skillLevel);
               SkillBonus longReach = SkillBonusRegistry.getSkillById("spears_long_reach");
               if (longReach != null && longReach.isActive(player)) {
                  SkillBonus.sendSkillActionBar(player, longReach);
               }

               if (PolearmMasterySkill.hasActiveSkill(uuid)) {
                  PolearmMasterySkill.applyAttackSpeedBonus(player, skillLevel);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         if (!SkillsConfig.isWorldExcludedFromSkills(event.getPlayer())) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
               if (event.getItem() != null) {
                  if (event.getItem().getType() == Material.BOW) {
                     UUID uuid = event.getPlayer().getUniqueId();
                     if (OverdrawSkill.hasActiveSkill(uuid)) {
                        OverdrawSkill.startDrawing(uuid);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onBowShoot(EntityShootBowEvent event) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         if (event.getEntity() instanceof Player player) {
            if (SkillsConfig.isWorldExcludedFromSkills(player)) {
               return;
            }

            if (event.isCancelled()) {
               OverdrawSkill.stopDrawing(player.getUniqueId());
            } else if (event.getProjectile() instanceof Projectile projectile) {
               OverdrawSkill.snapshotDrawDuration(player.getUniqueId(), projectile);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerMove(PlayerMoveEvent event) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         if (event.getTo() != null) {
            if (!SkillsConfig.isWorldExcludedFromSkills(event.getPlayer())) {
               UUID uuid = event.getPlayer().getUniqueId();
               boolean moved = event.getFrom().getX() != event.getTo().getX()
                  || event.getFrom().getY() != event.getTo().getY()
                  || event.getFrom().getZ() != event.getTo().getZ();
               if (SteadyAimSkill.hasActiveSkill(uuid)) {
                  SteadyAimSkill.updatePlayerMovement(uuid, moved);
               }

               if (moved) {
                  IronStanceSkill.updatePlayerMovement(uuid, true);
               }
            }
         }
      }
   }

   public static void shutdown() {
      PlayerSkillSelection.shutdown();
   }
}
