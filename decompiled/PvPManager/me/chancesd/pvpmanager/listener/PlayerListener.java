package me.chancesd.pvpmanager.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.chancesd.pvpmanager.event.PlayerCombatLogEvent;
import me.chancesd.pvpmanager.manager.DeathHandler;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.TimeUtil;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.ItemCooldown;
import me.chancesd.pvpmanager.setting.ItemKey;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

public class PlayerListener implements Listener {
   private final PlayerManager playerManager;
   private final DeathHandler deathHandler;
   private final Cache<UUID, String> msgCooldown = CacheBuilder.newBuilder().weakValues().expireAfterWrite(800L, TimeUnit.MILLISECONDS).build();

   public PlayerListener(PlayerManager pM) {
      this.playerManager = pM;
      this.deathHandler = new DeathHandler(pM);
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public final void onPlayerEat(PlayerItemConsumeEvent event) {
      CombatPlayer player = this.playerManager.get(event.getPlayer());
      if (Conf.BLOCK_EAT.asBool() && player.isInCombat() && event.getItem().getType().isEdible()) {
         event.setCancelled(true);
         player.message(Lang.EAT_BLOCKED_IN_COMBAT, new Object[0]);
      } else {
         ItemKey key = ItemKey.fromItemStack(event.getItem());
         ItemCooldown cooldown = Conf.getItemCooldown(key);
         if (cooldown != null) {
            if (player.hasItemCooldown(key)) {
               event.setCancelled(true);
               player.message(Lang.ITEM_COOLDOWN, new Object[]{TimeUtil.getDiffUntil(Lang.ITEM_COOLDOWN, player.getItemCooldown(key))});
               return;
            }

            player.setItemCooldown(key, cooldown);
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public final void onPlayerKick(PlayerKickEvent event) {
      Player player = event.getPlayer();
      Log.debugLazy(
         () -> player.getName()
            + " was kicked with reason: "
            + event.getReason()
            + " | Leave message: "
            + event.getLeaveMessage()
            + " - In combat: "
            + this.playerManager.get(player).isInCombat()
      );
      if (!Conf.PUNISH_ON_KICK.asBool()
         || Conf.MATCH_KICK_REASON.asBool()
            && !Conf.PUNISH_KICK_REASONS.asList().stream().anyMatch(reason -> event.getReason().toLowerCase().contains(reason.toLowerCase()))) {
         CombatPlayer pvPlayer = this.playerManager.get(player);
         if (pvPlayer.isInCombat()) {
            pvPlayer.untag(UntagReason.KICKED);
         }
      }
   }

   @EventHandler
   public final void onPlayerLogout(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      CombatPlayer combatPlayer = this.playerManager.get(player);
      Log.debug(player.getName() + " quit with message: " + event.getQuitMessage() + " - In combat: " + combatPlayer.isInCombat());
      if (combatPlayer.isInCombat()) {
         PlayerCombatLogEvent combatLogEvent = new PlayerCombatLogEvent(player, combatPlayer);
         Bukkit.getPluginManager().callEvent(combatLogEvent);
         if (combatPlayer.hasPerm(Permissions.EXEMPT_COMBAT_LOG) || combatLogEvent.isCancelled()) {
            return;
         }

         this.playerManager.applyPunishments(combatPlayer);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public final void onPlayerLogoutMonitor(PlayerQuitEvent event) {
      CombatPlayer player = this.playerManager.get(event.getPlayer());
      if (player.isInCombat()) {
         player.untag(UntagReason.LOGOUT);
      }

      ScheduleUtils.runPlatformTask(() -> this.playerManager.removePlayer(player));
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public final void onPlayerDeath(PlayerDeathEvent event) {
      if (!CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName())) {
         this.deathHandler.processDeath(event);
      }
   }

   @EventHandler
   public final void onPlayerCooldown(PlayerInteractEvent e) {
      Player player = e.getPlayer();
      if (!CombatUtils.isWorldExcluded(player.getWorld().getName())) {
         Material type = e.getMaterial();
         if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            ItemKey key = ItemKey.fromItemStack(e.getItem());
            ItemCooldown cooldown = Conf.getItemCooldown(key);
            if (cooldown != null) {
               CombatPlayer combatPlayer = this.playerManager.get(player);
               if (combatPlayer.hasItemCooldown(key)) {
                  String msg = Lang.ITEM_COOLDOWN.msgTimeUntil(combatPlayer.getItemCooldown(key));
                  if (!msg.equals(this.msgCooldown.getIfPresent(player.getUniqueId()))) {
                     combatPlayer.message(Lang.ITEM_COOLDOWN, new Object[]{TimeUtil.getDiffUntil(Lang.ITEM_COOLDOWN, combatPlayer.getItemCooldown(key))});
                     this.msgCooldown.put(player.getUniqueId(), msg);
                  }

                  e.setCancelled(true);
               } else if (!type.isEdible()) {
                  ScheduleUtils.runPlatformTask(() -> combatPlayer.setItemCooldown(key, cooldown));
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public final void onPlayerInteract(PlayerInteractEvent e) {
      Player player = e.getPlayer();
      if (!CombatUtils.isWorldExcluded(player.getWorld().getName()) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Block clickedBlock = e.getClickedBlock();
         if (clickedBlock != null) {
            CombatPlayer pvplayer = this.playerManager.get(player);
            Material type = e.getMaterial();
            if (type == Material.FLINT_AND_STEEL) {
               for (Player p : clickedBlock.getWorld().getPlayers()) {
                  if (!this.isInvalidAttack(player, p) && clickedBlock.getWorld().equals(p.getWorld())) {
                     CombatPlayer target = this.playerManager.get(p);
                     if ((!target.hasPvPEnabled() || !pvplayer.hasPvPEnabled()) && clickedBlock.getLocation().distanceSquared(p.getLocation()) < 9.0) {
                        pvplayer.message(Lang.ATTACK_DENIED_OTHER, new Object[]{target.getName()});
                        e.setCancelled(true);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public final void onBucketEmpty(PlayerBucketEmptyEvent event) {
      Player player = event.getPlayer();
      CombatPlayer combatPlayer = this.playerManager.get(player);
      Block clickedBlock = event.getBlockClicked();
      if (event.getBucket() == Material.LAVA_BUCKET) {
         for (Player p : clickedBlock.getWorld().getPlayers()) {
            if (!this.isInvalidAttack(player, p) && clickedBlock.getWorld().equals(p.getWorld())) {
               Location playerLocation = p.getLocation();
               if (playerLocation != null
                  && clickedBlock.getLocation().distanceSquared(playerLocation) < 25.0
                  && this.playerManager.checkProtection(player, p).isProtected()) {
                  combatPlayer.message(Lang.ATTACK_DENIED_OTHER, new Object[]{p.getName()});
                  event.setCancelled(true);
                  return;
               }
            }
         }
      }
   }

   public boolean isInvalidAttack(Player attacker, Player victim) {
      return victim == null || attacker.equals(victim) || !attacker.canSee(victim) || victim.getGameMode() == GameMode.SPECTATOR;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public final void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      CombatPlayer pvPlayer = this.playerManager.createPlayer(player, true);
      ScheduleUtils.runAsync(() -> {
         if (player.isOp() || pvPlayer.hasPerm(Permissions.ADMIN)) {
            Lang.sendQueuedMsgs(pvPlayer);
         }
      });
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public final void onPlayerFish(PlayerFishEvent event) {
      Player player = event.getPlayer();
      if (!CombatUtils.isWorldExcluded(player.getWorld().getName())) {
         if (event.getState() == State.CAUGHT_ENTITY && event.getCaught() instanceof Player caught) {
            ProtectionResult result = this.playerManager.checkProtection(player, caught);
            if (result.isProtected()) {
               event.setCancelled(true);
               event.getHook().setHookedEntity(null);
               Lang.messageProtection(result, player, caught);
            } else if (!Conf.IGNORE_NO_DMG_HITS.asBool()) {
               this.playerManager.getPlugin().getEntityListener().processDamage(player, caught);
            }
         }
      }
   }

   @EventHandler
   public final void onPlayerRespawn(PlayerRespawnEvent event) {
      Player player = event.getPlayer();
      if (!CombatUtils.isWorldExcluded(player.getWorld().getName())) {
         CombatPlayer combatPlayer = this.playerManager.getUnchecked(player);
         if (combatPlayer != null) {
            if (Conf.KILL_ABUSE_ENABLED.asBool() && Conf.RESPAWN_PROTECTION.asInt() != 0) {
               combatPlayer.setRespawnTime(System.currentTimeMillis());
            }

            if (combatPlayer.wasLastDeathPvP()) {
               CombatUtils.executeCommands(Conf.COMMANDS_ON_RESPAWN.asList(), player, player.getName());
            }
         }
      }
   }

   @EventHandler
   public void onChangeWorld(PlayerChangedWorldEvent event) {
      Player player = event.getPlayer();
      CombatPlayer combatPlayer = this.playerManager.getUnchecked(player);
      if (combatPlayer != null) {
         CombatWorld combatWorld = this.playerManager.getPlugin().getWorldManager().getWorld(player.getWorld());
         combatPlayer.setCombatWorld(combatWorld);
         if (combatPlayer.isNewbie() && combatPlayer.getNewbieTask() != null) {
            if (CombatUtils.isWorldExcluded(player.getWorld().getName())) {
               combatPlayer.getNewbieTask().pause();
            } else {
               combatPlayer.getNewbieTask().resume();
            }
         }

         CombatWorld.WorldOptionState optionState = combatPlayer.getCombatWorld().isPvPForced();
         if (optionState != CombatWorld.WorldOptionState.NONE) {
            if (combatPlayer.hasPvPEnabled() && optionState == CombatWorld.WorldOptionState.OFF) {
               combatPlayer.setPvP(false);
               combatPlayer.message(Lang.ERROR_PVP_TOGGLE_NO_PVP, new Object[0]);
            } else {
               if (!combatPlayer.hasPvPEnabled() && optionState == CombatWorld.WorldOptionState.ON) {
                  combatPlayer.setPvP(true);
                  combatPlayer.message(Lang.ERROR_PVP_TOGGLE_FORCE_PVP, new Object[0]);
               }
            }
         }
      }
   }
}
