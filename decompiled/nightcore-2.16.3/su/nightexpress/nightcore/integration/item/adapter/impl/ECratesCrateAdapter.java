package su.nightexpress.nightcore.integration.item.adapter.impl;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentcrates.CratesAPI;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;

public class ECratesCrateAdapter extends IdentifiableItemAdapter {
   public ECratesCrateAdapter() {
      super("excellentcrates_crate");
   }

   @Nullable
   @Override
   public String getItemId(@NonNull ItemStack itemStack) {
      if (!CratesAPI.isLoaded()) {
         return null;
      } else {
         Crate crate = CratesAPI.getCrateManager().getCrateByItem(itemStack);
         return crate == null ? null : crate.getId();
      }
   }

   @Nullable
   @Override
   public ItemStack createItem(@NonNull String itemId) {
      if (!CratesAPI.isLoaded()) {
         return null;
      } else {
         Crate crate = CratesAPI.getCrateManager().getCrateById(itemId);
         return crate == null ? null : crate.getItemStack();
      }
   }

   @Override
   public boolean canHandle(@NonNull ItemStack itemStack) {
      return CratesAPI.isLoaded() && CratesAPI.getCrateManager().getCrateByItem(itemStack) != null;
   }

   public boolean canHandle(@NonNull ItemIdData data) {
      return CratesAPI.isLoaded() && CratesAPI.getCrateManager().getCrateById(data.getItemId()) != null;
   }
}
