package com.donutorders.listener;

import com.donutorders.manager.ChatInputHandler;
import com.donutorders.manager.GUIManager;
import com.donutorders.manager.OrderLimitManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
   private final GUIManager guiManager;
   private final ChatInputHandler chatInputHandler;
   private final OrderLimitManager orderLimitManager;

   public PlayerQuitListener(GUIManager guiManager, ChatInputHandler chatInputHandler, OrderLimitManager orderLimitManager) {
      this.guiManager = guiManager;
      this.chatInputHandler = chatInputHandler;
      this.orderLimitManager = orderLimitManager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.chatInputHandler.cancelSession(player);
      this.guiManager.returnHeldDeliveryItems(player);
      this.guiManager.clearState(player.getUniqueId());
      this.orderLimitManager.evict(player.getUniqueId());
   }
}
