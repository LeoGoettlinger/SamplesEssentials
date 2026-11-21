package me.chancesd.pvpmanager.listener;

import java.util.Arrays;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.sdutils.utils.TimeUtil;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.ItemCooldown;
import me.chancesd.pvpmanager.setting.ItemKey;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class BlockedActionsListener implements Listener {
   private final PlayerManager playerHandler;
   Material fireworkMaterial;

   public BlockedActionsListener(PlayerManager ph) {
      this.playerHandler = ph;
      if (MCVersion.isAtLeast(MCVersion.V1_13)) {
         this.fireworkMaterial = Material.FIREWORK_ROCKET;
      } else {
         this.fireworkMaterial = Material.getMaterial("FIREWORK");
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public final void onBlockPlace(BlockPlaceEvent event) {
      if (Conf.BLOCK_PLACE_BLOCKS.asBool() || Conf.NEWBIE_BLOCK_PLACE.asBool()) {
         CombatPlayer combatPlayer = this.playerHandler.get(event.getPlayer());
         if (Conf.BLOCK_PLACE_BLOCKS.asBool() && combatPlayer.isInCombat() || Conf.NEWBIE_BLOCK_PLACE.asBool() && combatPlayer.isNewbie()) {
            event.setCancelled(true);
            combatPlayer.message(Lang.BLOCK_PLACE_BLOCKED_IN_COMBAT, new Object[0]);
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public final void onBlockBreak(BlockBreakEvent event) {
      if (Conf.BLOCK_BREAK_BLOCKS.asBool() || Conf.NEWBIE_BLOCK_BREAK.asBool()) {
         CombatPlayer combatPlayer = this.playerHandler.get(event.getPlayer());
         if (Conf.BLOCK_BREAK_BLOCKS.asBool() && combatPlayer.isInCombat() || Conf.NEWBIE_BLOCK_BREAK.asBool() && combatPlayer.isNewbie()) {
            event.setCancelled(true);
            combatPlayer.message(Lang.BLOCK_BREAK_BLOCKED_IN_COMBAT, new Object[0]);
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public final void onToggleFlight(PlayerToggleFlightEvent event) {
      if (Conf.DISABLE_FLY.asBool() && event.isFlying() && this.playerHandler.get(event.getPlayer()).isInCombat()) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public final void onFireworkUseWhileGliding(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      if (!MCVersion.isLowerThan(MCVersion.V1_9) && player.isGliding()) {
         Action action = event.getAction();
         if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            CombatPlayer pvPlayer = this.playerHandler.get(player);
            if (pvPlayer.isInCombat()) {
               ItemStack item = event.getItem();
               if (item != null && item.getType() == this.fireworkMaterial) {
                  if (Conf.BLOCK_FIREWORKS_IN_COMBAT.asBool()) {
                     event.setCancelled(true);
                     pvPlayer.message(Lang.FIREWORK_BLOCKED_IN_COMBAT, new Object[0]);
                  } else {
                     int powerLimit = Conf.FIREWORK_POWER_LIMIT.asInt();
                     if (powerLimit >= 0 && item.hasItemMeta()) {
                        FireworkMeta meta = (FireworkMeta)item.getItemMeta();
                        if (meta != null && meta.getPower() > powerLimit) {
                           event.setCancelled(true);
                           pvPlayer.message(Lang.FIREWORK_POWER_LIMITED_IN_COMBAT, new Object[]{meta.getPower()});
                        }
                     }

                     int elytraCooldown = Conf.FIREWORK_ELYTRA_COOLDOWN.asInt();
                     if (elytraCooldown >= 0) {
                        ItemKey fireworkKey = ItemKey.wildcard(this.fireworkMaterial);
                        if (pvPlayer.hasItemCooldown(fireworkKey)) {
                           event.setCancelled(true);
                           pvPlayer.message(Lang.ITEM_COOLDOWN, new Object[]{TimeUtil.getDiffUntil(Lang.ITEM_COOLDOWN, pvPlayer.getItemCooldown(fireworkKey))});
                           return;
                        }

                        ScheduleUtils.runPlatformTask(() -> pvPlayer.setItemCooldown(fireworkKey, new ItemCooldown(elytraCooldown, -1)));
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public final void onPlayerBlockInteract(PlayerInteractEvent e) {
      Player player = e.getPlayer();
      if (Conf.BLOCK_INTERACT_IN_COMBAT.asBool()
         && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.PHYSICAL)
         && !CombatUtils.isWorldExcluded(player.getWorld().getName())) {
         CombatPlayer combatPlayer = this.playerHandler.get(player);
         Block clickedBlock = e.getClickedBlock();
         if (clickedBlock != null) {
            if (combatPlayer.isInCombat()) {
               Material blockType = clickedBlock.getType();

               for (String material : Conf.BLOCK_INTERACT_ITEM_LIST.asList()) {
                  if (blockType.name().endsWith(material)) {
                     e.setCancelled(true);
                     combatPlayer.message(Lang.INTERACT_BLOCKED_IN_COMBAT, new Object[0]);
                     return;
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public final void onPlayerPickup(PlayerPickupItemEvent e) {
      if (Conf.NEWBIE_ENABLED.asBool() && Conf.NEWBIE_BLOCK_PICK.asBool()) {
         CombatPlayer player = this.playerHandler.get(e.getPlayer());
         if (player.isNewbie()) {
            e.setCancelled(true);
            player.message(Lang.NEWBIE_PICKUP_ITEM_BLOCKED, new Object[0]);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public final void onPlayerTeleport(PlayerTeleportEvent event) {
      Player player = event.getPlayer();
      CombatPlayer combatPlayer = this.playerHandler.getUnchecked(player);
      if (combatPlayer != null) {
         if (Conf.COMBAT_TAG_ENABLED.asBool() && combatPlayer.isInCombat()) {
            TeleportCause cause = event.getCause();
            if (cause.equals(TeleportCause.ENDER_PEARL) && Conf.BLOCK_ENDERPEARL.asBool()) {
               event.setCancelled(true);
               combatPlayer.message(Lang.ENDERPEARL_BLOCKED_INCOMBAT, new Object[0]);
            } else if (MCVersion.isAtLeast(MCVersion.V1_9) && cause == TeleportCause.CHORUS_FRUIT && Conf.BLOCK_CHORUSFRUIT.asBool()) {
               event.setCancelled(true);
               combatPlayer.message(Lang.CHORUS_BLOCKED_IN_COMBAT, new Object[0]);
            } else if (this.shouldBlockTeleport(cause) && !combatPlayer.getExemptions().canBypassTeleportBlock()) {
               event.setCancelled(true);
               combatPlayer.message(Lang.TELEPORT_BLOCKED_IN_COMBAT, new Object[0]);
            }
         }
      }
   }

   private boolean shouldBlockTeleport(TeleportCause cause) {
      return cause.equals(TeleportCause.COMMAND) && Conf.BLOCK_TELEPORT.asBool()
         || (cause.equals(TeleportCause.PLUGIN) || cause.equals(TeleportCause.UNKNOWN)) && Conf.BLOCK_UNSAFE_TELEPORTS.asBool();
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public final void onCommand(PlayerCommandPreprocessEvent event) {
      if (Conf.COMBAT_TAG_ENABLED.asBool() && Conf.BLOCK_COMMANDS.asBool() || Conf.NEWBIE_ENABLED.asBool()) {
         CombatPlayer player = this.playerHandler.get(event.getPlayer());
         String[] givenCommand = event.getMessage().substring(1).split(" ", 3);
         if (player.isInCombat() && !player.hasPerm(Permissions.EXEMPT_BLOCK_COMMANDS)) {
            boolean contains = CombatUtils.recursiveContainsCommand(givenCommand, Conf.COMMANDS_ALLOWED.asList());
            Log.debug(
               "Command: "
                  + Arrays.<String>asList(givenCommand)
                  + " | Whitelist: "
                  + Conf.BLOCK_COMMANDS_WHITELIST.asBool()
                  + " | In list: "
                  + contains
                  + " | Blocked: "
                  + (Conf.BLOCK_COMMANDS_WHITELIST.asBool() != contains)
            );
            if (Conf.BLOCK_COMMANDS_WHITELIST.asBool() != contains) {
               event.setCancelled(true);
               player.message(Lang.COMMAND_DENIED_INCOMBAT, new Object[0]);
            }
         }

         if (player.isNewbie() && CombatUtils.recursiveContainsCommand(givenCommand, Conf.NEWBIE_BLACKLIST.asList())) {
            event.setCancelled(true);
            player.message(Lang.NEWBIE_COMMAND_BLOCKED, new Object[0]);
         }
      }
   }

   @EventHandler
   public final void onInventoryOpen(InventoryOpenEvent event) {
      if (Conf.BLOCK_INVENTORY_OPEN.asBool()) {
         CombatPlayer combatPlayer = this.playerHandler.get((Player)event.getPlayer());
         if (combatPlayer.isInCombat()) {
            event.setCancelled(true);
            combatPlayer.message(Lang.INVENTORY_BLOCKED_IN_COMBAT, new Object[0]);
         }
      }
   }
}
