package com.Sw3ys.bigenderchest.listeners;

import com.Sw3ys.bigenderchest.manager.EnderChestManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DataListener implements Listener {
   private final EnderChestManager manager;

   public DataListener(EnderChestManager manager) {
      this.manager = manager;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent e) {
      this.manager.reconcileJoin(e.getPlayer());
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      this.manager.snapshotQuit(e.getPlayer());
   }
}
