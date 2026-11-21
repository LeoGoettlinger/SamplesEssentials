package su.nightexpress.nightcore.integration.item.adapter.impl;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentcrates.CratesAPI;
import su.nightexpress.excellentcrates.key.CrateKey;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;

public class ECratesKeyAdapter extends IdentifiableItemAdapter {
   public ECratesKeyAdapter() {
      super("excellentcrates_key");
   }

   @Nullable
   @Override
   public String getItemId(@NonNull ItemStack itemStack) {
      if (!CratesAPI.isLoaded()) {
         return null;
      } else {
         CrateKey key = CratesAPI.getKeyManager().getKeyByItem(itemStack);
         return key == null ? null : key.getId();
      }
   }

   @Nullable
   @Override
   public ItemStack createItem(@NonNull String itemId) {
      if (!CratesAPI.isLoaded()) {
         return null;
      } else {
         CrateKey key = CratesAPI.getKeyManager().getKeyById(itemId);
         return key == null ? null : key.getItemStack();
      }
   }

   @Override
   public boolean canHandle(@NonNull ItemStack itemStack) {
      return CratesAPI.isLoaded() && CratesAPI.getKeyManager().getKeyByItem(itemStack) != null;
   }

   public boolean canHandle(@NonNull ItemIdData data) {
      return CratesAPI.isLoaded() && CratesAPI.getKeyManager().getKeyById(data.getItemId()) != null;
   }
}
