package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PacketEntityEventListener implements Listener {
   private final PacketEntityTracker tracker;

   public PacketEntityEventListener(PacketEntityTracker tracker) {
      this.tracker = tracker;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      this.tracker.onPlayerJoin(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.tracker.onPlayerQuit(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      PacketPassengerRegistry.clearViewer(event.getPlayer().getUniqueId());
      this.tracker.onPlayerRespawn(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      PacketPassengerRegistry.clearViewer(event.getPlayer().getUniqueId());
      World fromWorld = event.getFrom();
      this.tracker.onPlayerChangedWorld(event.getPlayer(), fromWorld);
   }
}
