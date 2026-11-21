package com.artillexstudios.axtrade.listeners;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.request.Request;
import com.artillexstudios.axtrade.request.Requests;
import com.artillexstudios.axtrade.trade.Trade;
import com.artillexstudios.axtrade.trade.Trades;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class TradeListeners implements Listener {
   @EventHandler
   public void onQuit(@NotNull PlayerQuitEvent event) {
      this.handleQuitTrade(event);
      this.handleQuitRequest(event);
   }

   public void handleQuitTrade(@NotNull PlayerQuitEvent event) {
      Player player = event.getPlayer();
      Trade trade = Trades.getTrade(player);
      if (trade != null) {
         trade.abort();
      }
   }

   public void handleQuitRequest(@NotNull PlayerQuitEvent event) {
      Iterator<Request> iterator = Requests.getRequests().iterator();

      while (iterator.hasNext()) {
         Request request = iterator.next();
         if (request.getSender().equals(event.getPlayer())) {
            iterator.remove();
         } else if (request.getReceiver().equals(event.getPlayer())) {
            iterator.remove();
            if (request.isActive()) {
               AxTrade.MESSAGEUTILS.sendLang(request.getSender(), "request.expired", Map.of("%player%", request.getReceiver().getName()));
            }
         }
      }
   }

   @EventHandler
   public void onDrop(@NotNull PlayerDropItemEvent event) {
      this.cancelIfTrading(event.getPlayer(), event);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPickup(@NotNull EntityPickupItemEvent event) {
      if (event.getEntity() instanceof Player player) {
         this.cancelIfTrading(player, event);
      }
   }

   @EventHandler
   public void onArrowPickup(@NotNull PlayerPickupArrowEvent event) {
      this.cancelIfTrading(event.getPlayer(), event);
   }

   @EventHandler
   public void onMove(@NotNull PlayerMoveEvent event) {
      if (event.getTo() != null) {
         Trade trade = Trades.getTrade(event.getPlayer());
         if (trade != null) {
            if (AxTrade.CONFIG.getBoolean("abort.move", true)) {
               if (System.currentTimeMillis() - trade.getPrepTime() >= 1000L) {
                  if (event.getFrom().distanceSquared(event.getTo()) != 0.0) {
                     trade.abort();
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onInteract(@NotNull PlayerInteractEvent event) {
      Trade trade = Trades.getTrade(event.getPlayer());
      if (trade != null) {
         if (AxTrade.CONFIG.getBoolean("abort.interact", true)) {
            if (System.currentTimeMillis() - trade.getPrepTime() >= 1000L) {
               event.setCancelled(true);
               trade.abort();
            }
         }
      }
   }

   @EventHandler
   public void onCommand(@NotNull PlayerCommandPreprocessEvent event) {
      Trade trade = Trades.getTrade(event.getPlayer());
      if (trade != null) {
         if (AxTrade.CONFIG.getBoolean("abort.command", true)) {
            event.setCancelled(true);
            trade.abort();
         }
      }
   }

   private void cancelIfTrading(Player player, Cancellable event) {
      Trade trade = Trades.getTrade(player);
      if (trade != null) {
         event.setCancelled(true);
      }
   }
}
