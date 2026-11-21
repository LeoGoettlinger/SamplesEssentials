package com.oheers.fish.baits.manager;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.baits.AbstractBaitManager;
import com.oheers.fish.api.baits.IBait;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.configs.BaitConversions;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.items.nbt.NbtKeys;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BaitManager extends AbstractBaitManager<BaitHandler> {
   private static BaitManager instance;

   private BaitManager() {
   }

   @NonNull
   public static BaitManager getInstance() {
      if (instance == null) {
         instance = new BaitManager();
      }

      return instance;
   }

   @Override
   protected void performPreLoadConversions() {
      new BaitConversions().performCheck();
   }

   @Override
   protected void loadItems() {
      this.loadItemsFromFiles("baits", file -> new BaitHandler(file, FishManager.getInstance(), MainConfig.getInstance()), BaitHandler::getId, bait -> {
         if (bait.getBaitData().disabled()) {
            EvenMoreFish.getInstance().debug("Skipping disabled bait: " + bait.getId());
            return true;
         } else {
            if (bait.getRecipe() != null) {
               bait.getRecipe().register();
            }

            return false;
         }
      });
   }

   @Override
   protected void clearMap(boolean reload) {
      if (reload) {
         this.itemMap.forEach((id, bait) -> {
            if (bait.getRecipe() != null) {
               bait.getRecipe().unregister();
            }
         });
      }

      super.clearMap(reload);
   }

   @Override
   protected void logLoadedItems() {
      EvenMoreFish.getInstance().getLogger().info("Loaded " + this.getItemMap().size() + " baits successfully.");
   }

   @Nullable
   public BaitHandler getBait(@Nullable String baitName) {
      return baitName != null ? this.getItem(baitName) : null;
   }

   @Nullable
   public BaitHandler getBait(@Nullable ItemStack itemStack) {
      return itemStack != null ? this.getBait(BaitNBTManager.getBaitName(itemStack)) : null;
   }

   @Nullable
   @Override
   public IBait getBait(@Nullable Entity itemEntity) {
      return itemEntity instanceof Item item ? this.getBait(item.getItemStack()) : null;
   }

   @Override
   public boolean isBait(@Nullable ItemStack itemStack) {
      return itemStack != null && !itemStack.isEmpty() && NBTHolder.itemStack(itemStack).hasKey(NbtKeys.EMF_BAIT.get());
   }

   @Override
   public boolean isBait(@Nullable Entity itemEntity) {
      return itemEntity instanceof Item item ? this.isBait(item.getItemStack()) : false;
   }
}
