package com.oheers.fish.fishing.rods;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.fishing.rods.AbstractRodManager;
import com.oheers.fish.items.nbt.NbtKeys;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RodManager extends AbstractRodManager<CustomRod> {
   private static final RodManager instance = new RodManager();

   private RodManager() {
   }

   @NonNull
   public static RodManager getInstance() {
      return instance;
   }

   @Override
   protected void performPreLoadConversions() {
      new RodConversion().performCheck();
   }

   @Override
   protected void loadItems() {
      this.performPreLoadConversions();
      this.loadItemsFromFiles("rods", CustomRod::new, CustomRod::getId, rod -> {
         if (rod.isDisabled()) {
            return true;
         } else {
            if (rod.getRecipe() != null) {
               rod.getRecipe().register();
            }

            return false;
         }
      });
   }

   @Override
   protected void logLoadedItems() {
      EvenMoreFish.getInstance().getLogger().info("Loaded RodManager with " + this.itemMap.size() + " Custom Rods.");
   }

   @Override
   protected void clearMap(boolean reload) {
      if (reload) {
         this.itemMap.forEach((id, rod) -> {
            if (rod.getRecipe() != null) {
               rod.getRecipe().unregister();
            }
         });
      }

      super.clearMap(reload);
   }

   @Nullable
   public CustomRod getRod(@Nullable ItemStack item) {
      if (item != null && !item.isEmpty()) {
         NBTHolder<ItemStack> holder = NBTHolder.itemStack(item);
         String rodId = holder.getString(NbtKeys.EMF_ROD_ID.get());
         if (rodId != null) {
            return this.getItem(rodId);
         } else {
            return holder.hasKey(NbtKeys.EMF_ROD_NBT.get()) ? this.getItem("default") : null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   public CustomRod getRod(@NonNull String rodId) {
      return this.getItem(rodId);
   }
}
