package com.donutorders.listener;

import com.donutorders.DonutOrders;
import com.donutorders.manager.OrderLimitManager;
import com.donutorders.util.MessageHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
   private final OrderLimitManager orderLimitManager;

   public PlayerJoinListener(OrderLimitManager orderLimitManager) {
      this.orderLimitManager = orderLimitManager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      this.orderLimitManager.refresh(player);
      DonutOrders plugin = DonutOrders.getInstance();
      if (plugin != null && plugin.hasUpdateAvailable()) {
         if (plugin.getConfig().getBoolean("update-check.notify-ops-on-join", true)) {
            if (player.hasPermission("donutorders.admin")) {
               player.sendMessage(
                  MessageHelper.get(
                     "update-available", "&e[DonutOrders] &7Update available: &f{0} &7— &b{1}", plugin.getUpdateLatestVersion(), plugin.getUpdateDownloadUrl()
                  )
               );
            }
         }
      }
   }
}
