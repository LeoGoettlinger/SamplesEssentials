package su.nightexpress.nightcore.integration.item.adapter.impl;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;

public class ItemsAdderAdapter extends IdentifiableItemAdapter {
   public ItemsAdderAdapter() {
      super("itemsadder");
   }

   @Nullable
   @Override
   public ItemStack createItem(@NonNull String itemId) {
      CustomStack customStack = CustomStack.getInstance(itemId);
      return customStack == null ? null : customStack.getItemStack();
   }

   @Override
   public boolean canHandle(@NonNull ItemStack itemStack) {
      return CustomStack.byItemStack(itemStack) != null;
   }

   public boolean canHandle(@NonNull ItemIdData data) {
      return CustomStack.isInRegistry(data.getItemId());
   }

   @Nullable
   @Override
   public String getItemId(@NonNull ItemStack item) {
      if (item.getType().isAir()) {
         return null;
      } else {
         CustomStack stack = CustomStack.byItemStack(item);
         return stack == null ? null : stack.getNamespacedID();
      }
   }
}
