package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.config.serializer.ItemSerializer;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class ItemRewardType extends RewardType {
   @Override
   public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
      String[] parsedItem = value.split(",");
      ItemStack item = ItemSerializer.get().deserialize(parsedItem[0], true);
      if (item == null) {
         EvenMoreFish.getInstance().getLogger().warning("Invalid item specified for RewardType " + this.getIdentifier() + ": " + parsedItem[0]);
      } else {
         int quantity = 1;
         if (parsedItem.length > 1) {
            try {
               quantity = Integer.parseInt(parsedItem[1]);
            } catch (NumberFormatException var9) {
               EvenMoreFish.getInstance().getLogger().warning("Invalid quantity specified for RewardType " + this.getIdentifier() + ": " + parsedItem[1]);
               return;
            }
         }

         for (int i = 0; i < quantity; i++) {
            FishUtils.giveItem(item, player);
         }
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "ITEM";
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "Oheers";
   }

   @NonNull
   public JavaPlugin getPlugin() {
      return EvenMoreFish.getInstance();
   }
}
