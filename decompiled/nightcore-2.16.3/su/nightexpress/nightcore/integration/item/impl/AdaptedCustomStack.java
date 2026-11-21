package su.nightexpress.nightcore.integration.item.impl;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;

public class AdaptedCustomStack extends AdaptedItemStack<ItemIdData> {
   public AdaptedCustomStack(@NonNull ItemAdapter<ItemIdData> adapter, @NonNull ItemIdData data) {
      super(adapter, data);
   }

   @Override
   public boolean isValid() {
      return this.adapter.canHandle(this.data);
   }

   @Override
   public int getAmount() {
      return this.data.getAmount();
   }

   @Nullable
   @Override
   public ItemStack getItemStack() {
      return this.adapter.toItemStack(this.data);
   }

   public boolean isSimilar(@NonNull ItemIdData other) {
      return this.data.getItemId().equalsIgnoreCase(other.getItemId());
   }
}
