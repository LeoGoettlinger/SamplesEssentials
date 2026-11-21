package su.nightexpress.nightcore.integration.item.adapter.impl;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentcrates.CratesAPI;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.key.CrateKey;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;

@Deprecated
public class ExcellentCratesHandler extends IdentifiableItemAdapter {
   private static final String PREFIX_CRATE = "crate_";
   private static final String PREFIX_KEY = "key_";

   public ExcellentCratesHandler() {
      super("excellentcrates");
   }

   @Override
   public int getWeight() {
      return 1;
   }

   @Override
   public boolean canHandle(@NonNull ItemStack item) {
      return !CratesAPI.isLoaded() ? false : CratesAPI.getCrateManager().isCrate(item) || CratesAPI.getKeyManager().isKey(item);
   }

   @Nullable
   @Override
   public ItemStack createItem(@NonNull String itemId) {
      if (!CratesAPI.isLoaded()) {
         return null;
      } else if (itemId.startsWith("crate_")) {
         String id = itemId.substring("crate_".length());
         Crate crate = CratesAPI.getCrateManager().getCrateById(id);
         return crate == null ? null : crate.getItemStack();
      } else if (itemId.startsWith("key_")) {
         String id = itemId.substring("key_".length());
         CrateKey key = CratesAPI.getKeyManager().getKeyById(id);
         return key == null ? null : key.getItemStack();
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public String getItemId(@NonNull ItemStack item) {
      if (!CratesAPI.isLoaded()) {
         return null;
      } else {
         Crate crate = CratesAPI.getCrateManager().getCrateByItem(item);
         if (crate != null) {
            return "crate_" + crate.getId();
         } else {
            CrateKey key = CratesAPI.getKeyManager().getKeyByItem(item);
            return key != null ? "key_" + key.getId() : null;
         }
      }
   }

   public boolean canHandle(@NonNull ItemIdData data) {
      if (!CratesAPI.isLoaded()) {
         return false;
      } else {
         String itemId = data.getItemId();
         if (itemId.startsWith("crate_")) {
            String id = itemId.substring("crate_".length());
            return CratesAPI.getCrateManager().getCrateById(id) != null;
         } else if (itemId.startsWith("key_")) {
            String id = itemId.substring("key_".length());
            return CratesAPI.getKeyManager().getKeyById(id) != null;
         } else {
            return false;
         }
      }
   }
}
