package com.oheers.fish.events;

import com.oheers.fish.api.EMFFishEvent;
import com.oheers.fish.api.EMFFishSellEvent;
import com.oheers.fish.api.events.EMFFishCaughtEvent;
import com.oheers.fish.api.events.EMFFishHuntEvent;
import com.oheers.fish.api.events.EMFFishPreSaleEvent;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.selling.SoldFish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DeprecatedEventListener implements Listener {
   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.HIGHEST
   )
   public void onCaughtEvent(EMFFishCaughtEvent event) {
      if (event.getFish() instanceof Fish emfFish) {
         EMFFishEvent deprecated = new EMFFishEvent(emfFish, event.getPlayer(), event.getCatchTime());
         if (!deprecated.callEvent()) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.HIGHEST
   )
   public void onHuntEvent(EMFFishHuntEvent event) {
      if (event.getFish() instanceof Fish emfFish) {
         com.oheers.fish.api.EMFFishHuntEvent deprecated = new com.oheers.fish.api.EMFFishHuntEvent(emfFish, event.getPlayer(), event.getHuntTime());
         if (!deprecated.callEvent()) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.HIGHEST
   )
   public void onSoldEvent(EMFFishPreSaleEvent event) {
      SoldFish deprecatedFish = new SoldFish(event.getSoldFish());
      EMFFishSellEvent deprecated = new EMFFishSellEvent(deprecatedFish, event.getPlayer(), deprecatedFish.getTotalValue(), event.getSoldFish().getSellTime());
      if (!deprecated.callEvent()) {
         event.getSoldFish().setValue(-1.0);
      }
   }
}
