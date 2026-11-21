package com.sk89q.worldedit.bukkit;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.util.UpdateNotification;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.SessionIdleEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.event.InteractionDebouncer;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import java.util.Optional;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.inject.InjectedValueStore;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.inject.MapBackedValueStore;

public class WorldEditListener implements Listener {
   private final WorldEditPlugin plugin;
   private final InteractionDebouncer debouncer;

   public WorldEditListener(WorldEditPlugin plugin) {
      this.plugin = plugin;
      this.debouncer = new InteractionDebouncer(plugin.getInternalPlatform());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onGamemode(PlayerGameModeChangeEvent event) {
      if (this.plugin.getInternalPlatform().isHookingEvents()) {
         Player player = this.plugin.wrapPlayer(event.getPlayer());
         LocalSession session;
         if ((session = WorldEdit.getInstance().getSessionManager().getIfPresent(player)) == null) {
            session = WorldEdit.getInstance().getSessionManager().get(player);
         }

         session.loadDefaults(player, true);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onJoin(PlayerJoinEvent event) {
      if (this.plugin.getInternalPlatform().isHookingEvents()) {
         BukkitPlayer player = this.plugin.wrapPlayer(event.getPlayer());
         if (player.getPlayer() != event.getPlayer()) {
            player = this.plugin.reCachePlayer(event.getPlayer());
         }

         LocalSession session;
         if ((session = WorldEdit.getInstance().getSessionManager().getIfPresent(player)) != null) {
            session.loadDefaults(player, true);
         }

         if (Settings.settings().ENABLED_COMPONENTS.NOTIFY_UPDATE_INGAME) {
            UpdateNotification.doUpdateNotification(player);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerCommandSend(PlayerCommandSendEvent event) {
      InjectedValueStore store = MapBackedValueStore.create();
      store.injectValue(Key.of(Actor.class), context -> Optional.of(this.plugin.wrapCommandSender(event.getPlayer())));
      CommandManager commandManager = this.plugin.getWorldEdit().getPlatformManager().getPlatformCommandManager().getCommandManager();
      event.getCommands().removeIf(name -> commandManager.getCommand(name).filter(command -> !command.getCondition().satisfied(store)).isPresent());
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (this.plugin.getInternalPlatform().isHookingEvents()
         && event.useItemInHand() != Result.DENY
         && event.getHand() != EquipmentSlot.OFF_HAND
         && event.getAction() != Action.PHYSICAL) {
         Player player = this.plugin.wrapPlayer(event.getPlayer());
         if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            Optional<Boolean> previousResult = this.debouncer.getDuplicateInteractionResult(player);
            if (previousResult.isPresent()) {
               if (previousResult.get()) {
                  event.setCancelled(true);
               }

               return;
            }
         }

         World world = player.getWorld();
         WorldEdit we = this.plugin.getWorldEdit();
         Direction direction = BukkitAdapter.adapt(event.getBlockFace());
         Block clickedBlock = event.getClickedBlock();
         Location pos = clickedBlock == null ? null : new Location(world, clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());
         boolean result = false;
         switch (event.getAction()) {
            case LEFT_CLICK_BLOCK:
               result = we.handleBlockLeftClick(player, pos, direction) || we.handleArmSwing(player);
               break;
            case LEFT_CLICK_AIR:
               result = we.handleArmSwing(player);
               break;
            case RIGHT_CLICK_BLOCK:
               result = we.handleBlockRightClick(player, pos, direction) || we.handleRightClick(player);
               break;
            case RIGHT_CLICK_AIR:
               result = we.handleRightClick(player);
         }

         this.debouncer.setLastInteraction(player, result);
         if (result) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.debouncer.clearInteraction(this.plugin.wrapPlayer(event.getPlayer()));
      this.plugin.getWorldEdit().getEventBus().post(new SessionIdleEvent(new BukkitPlayer.SessionKeyImpl(event.getPlayer())));
   }
}
