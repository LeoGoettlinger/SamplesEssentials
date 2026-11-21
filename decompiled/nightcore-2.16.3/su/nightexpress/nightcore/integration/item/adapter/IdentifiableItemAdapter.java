package su.nightexpress.nightcore.integration.item.adapter;

import java.util.Optional;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;
import su.nightexpress.nightcore.integration.item.impl.AdaptedCustomStack;

public abstract class IdentifiableItemAdapter extends AbstractItemAdapter<ItemIdData> {
   public IdentifiableItemAdapter(@NonNull String name) {
      super(name);
   }

   @Override
   public int getWeight() {
      return 10;
   }

   @Nullable
   public AdaptedCustomStack readItem(@NonNull FileConfig config, @NonNull String path) {
      ItemIdData idData = ItemIdData.read(config, path);
      return new AdaptedCustomStack(this, idData);
   }

   @NonNull
   @Override
   public Optional<AdaptedCustomStack> adapt(@NonNull ItemStack itemStack) {
      ItemIdData data = this.fromItemStack(itemStack);
      return data == null ? Optional.empty() : Optional.of(new AdaptedCustomStack(this, data));
   }

   @Nullable
   public abstract String getItemId(@NonNull ItemStack var1);

   @Nullable
   public abstract ItemStack createItem(@NonNull String var1);

   @Nullable
   public ItemStack toItemStack(@NonNull ItemIdData data) {
      ItemStack itemStack = this.createItem(data.getItemId());
      if (itemStack == null) {
         return null;
      } else {
         itemStack.setAmount(data.getAmount());
         return itemStack;
      }
   }

   @Nullable
   public ItemIdData fromItemStack(@NonNull ItemStack itemStack) {
      String id = this.getItemId(itemStack);
      return id == null ? null : new ItemIdData(id, itemStack.getAmount());
   }
}
