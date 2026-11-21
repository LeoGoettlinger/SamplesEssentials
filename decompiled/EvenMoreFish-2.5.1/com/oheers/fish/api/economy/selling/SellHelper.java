package com.oheers.fish.api.economy.selling;

import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.events.EMFFishPreSaleEvent;
import com.oheers.fish.api.events.EMFFishSoldEvent;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public class SellHelper {
   private static final SellHelper GLOBAL = new SellHelper();
   @NonNull
   private final Economy economy;

   public SellHelper() {
      this(Economy.getInstance());
   }

   @NonNull
   public static SellHelper get() {
      return GLOBAL;
   }

   public SellHelper(@NonNull Economy economy) {
      this.economy = economy;
   }

   public void sell(@NonNull Inventory inventory, @NonNull Player player) {
      if (!this.economy.isEnabled()) {
         EMFPlugin.getInstance().sendMessage("ECONOMY_DISABLED", player);
      } else {
         boolean soldAny = false;
         double totalValue = 0.0;
         int count = 0;
         int size = inventory.getSize();

         for (int slot = 0; slot < size; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.isEmpty()) {
               SoldFish sold = SoldFish.get(player, item);
               if (sold != null) {
                  boolean cancelled = !new EMFFishPreSaleEvent(player, sold).callEvent();
                  if (!cancelled && !(sold.getValue() < 0.0)) {
                     this.logSoldFish(sold);
                     sold.getFish().getSellRewards().forEach(reward -> reward.give(player, player.getLocation()));
                     new EMFFishSoldEvent(player, sold).callEvent();
                     inventory.setItem(slot, null);
                     soldAny = true;
                     totalValue += sold.getFinalValue();
                     count += sold.getQuantity();
                  }
               }
            }
         }

         if (!soldAny) {
            EMFPlugin.getInstance().sendMessage("NO_SELLABLE_FISH", player);
         } else {
            this.economy.deposit(player, totalValue, true);
            EMFPlugin.getInstance().sendSoldMessage(totalValue, count, player);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.06F);
         }
      }
   }

   public void sell(@NonNull ItemStack[] itemStacks, @NonNull Player player) {
      if (!this.economy.isEnabled()) {
         EMFPlugin.getInstance().sendMessage("ECONOMY_DISABLED", player);
      } else {
         boolean soldAny = false;
         double totalValue = 0.0;
         int count = 0;

         for (ItemStack item : itemStacks) {
            if (item != null && !item.isEmpty()) {
               SoldFish sold = SoldFish.get(player, item);
               if (sold != null) {
                  boolean cancelled = !new EMFFishPreSaleEvent(player, sold).callEvent();
                  if (!cancelled && !(sold.getValue() < 0.0)) {
                     this.logSoldFish(sold);
                     sold.getFish().getSellRewards().forEach(reward -> reward.give(player, player.getLocation()));
                     new EMFFishSoldEvent(player, sold).callEvent();
                     item.setAmount(0);
                     soldAny = true;
                     totalValue += sold.getFinalValue();
                     count += sold.getQuantity();
                  }
               }
            }
         }

         if (!soldAny) {
            EMFPlugin.getInstance().sendMessage("NO_SELLABLE_FISH", player);
         } else {
            this.economy.deposit(player, totalValue, true);
            EMFPlugin.getInstance().sendSoldMessage(totalValue, count, player);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.06F);
         }
      }
   }

   private void logSoldFish(@NonNull SoldFish soldFish) {
      EMFPlugin.getInstance().logSoldFish(soldFish);
   }
}
