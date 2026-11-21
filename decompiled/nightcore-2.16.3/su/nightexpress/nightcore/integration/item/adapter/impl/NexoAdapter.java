package su.nightexpress.nightcore.integration.item.adapter.impl;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;

public class NexoAdapter extends IdentifiableItemAdapter {
   public NexoAdapter() {
      super("nexo");
   }

   @Nullable
   @Override
   public ItemStack createItem(@NonNull String itemId) {
      ItemBuilder builder = NexoItems.itemFromId(itemId);
      return builder == null ? null : builder.build();
   }

   @Override
   public boolean canHandle(@NonNull ItemStack itemStack) {
      return NexoItems.exists(itemStack);
   }

   public boolean canHandle(@NonNull ItemIdData data) {
      return NexoItems.exists(data.getItemId());
   }

   @Nullable
   @Override
   public String getItemId(@NonNull ItemStack item) {
      return NexoItems.idFromItem(item);
   }
}
