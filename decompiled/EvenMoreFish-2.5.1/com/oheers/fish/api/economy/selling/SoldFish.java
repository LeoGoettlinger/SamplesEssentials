package com.oheers.fish.api.economy.selling;

import com.oheers.fish.api.fishing.items.AbstractFishManager;
import com.oheers.fish.api.fishing.items.IFish;
import java.time.LocalDateTime;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SoldFish {
   @NonNull
   private final IFish fish;
   @Nullable
   private final Player player;
   @NonNull
   private final LocalDateTime sellTime;
   private int quantity;
   private double value;

   private SoldFish(@NonNull IFish fish, @Nullable Player player, int quantity, double value, @NonNull LocalDateTime sellTime) {
      this.fish = fish;
      this.player = player;
      this.sellTime = sellTime;
      this.value = value;
      this.quantity = quantity;
   }

   @Nullable
   public static SoldFish get(@Nullable Player player, @Nullable ItemStack item) {
      if (item != null && !item.isEmpty()) {
         IFish fish = AbstractFishManager.getInstance().getFish(item);
         if (fish == null) {
            return null;
         } else {
            int amount = item.getAmount();
            double setWorth = fish.getSetWorth();
            if (setWorth > 0.0) {
               return new SoldFish(fish, player, amount, setWorth, LocalDateTime.now());
            } else if (fish.isLengthless()) {
               return null;
            } else {
               double multiplier = fish.getWorthMultiplier();
               double worth = multiplier <= 0.0 ? -1.0 : multiplier * fish.getLength();
               return new SoldFish(fish, player, amount, worth, LocalDateTime.now());
            }
         }
      } else {
         return null;
      }
   }

   @NonNull
   public IFish getFish() {
      return this.fish.createCopy();
   }

   @Nullable
   public Player getPlayer() {
      return this.player;
   }

   public int getQuantity() {
      return this.quantity;
   }

   public void setQuantity(int quantity) {
      this.quantity = Math.min(1, quantity);
   }

   public double getFinalValue() {
      return this.value * this.quantity;
   }

   public double getValue() {
      return this.value;
   }

   public void setValue(double value) {
      this.value = value;
   }

   @NonNull
   public LocalDateTime getSellTime() {
      return this.sellTime;
   }
}
