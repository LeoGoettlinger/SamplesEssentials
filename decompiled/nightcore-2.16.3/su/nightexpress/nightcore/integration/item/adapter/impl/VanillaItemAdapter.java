package su.nightexpress.nightcore.integration.item.adapter.impl;

import java.util.Optional;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.item.adapter.AbstractItemAdapter;
import su.nightexpress.nightcore.integration.item.impl.AdaptedVanillaStack;
import su.nightexpress.nightcore.util.ItemTag;

public class VanillaItemAdapter extends AbstractItemAdapter<ItemTag> {
   public static final VanillaItemAdapter INSTANCE = new VanillaItemAdapter();

   private VanillaItemAdapter() {
      super("vanilla");
   }

   @Override
   public int getWeight() {
      return 0;
   }

   @Nullable
   public AdaptedVanillaStack readItem(@NonNull FileConfig config, @NonNull String path) {
      ItemTag itemTag = ItemTag.read(config, path);
      return new AdaptedVanillaStack(itemTag);
   }

   @NonNull
   @Override
   public Optional<AdaptedVanillaStack> adapt(@NonNull ItemStack itemStack) {
      ItemTag tag = this.fromItemStack(itemStack);
      return Optional.of(new AdaptedVanillaStack(tag));
   }

   @Override
   public boolean canHandle(@NonNull ItemStack itemStack) {
      return true;
   }

   public boolean canHandle(@NonNull ItemTag data) {
      return this.toItemStack(data) != null;
   }

   @Nullable
   public ItemStack toItemStack(@NonNull ItemTag data) {
      return data.getItemStack();
   }

   @NonNull
   public ItemTag fromItemStack(@NonNull ItemStack itemStack) {
      return ItemTag.of(itemStack);
   }
}
