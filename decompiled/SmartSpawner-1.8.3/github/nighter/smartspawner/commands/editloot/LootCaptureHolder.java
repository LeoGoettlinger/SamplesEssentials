package github.nighter.smartspawner.commands.editloot;

import lombok.Generated;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class LootCaptureHolder implements InventoryHolder {
   private final LootEditorTarget target;
   private final LootCaptureHolder.Purpose purpose;
   private final String entryKey;
   private final String lootKey;

   public LootCaptureHolder(LootEditorTarget target, LootCaptureHolder.Purpose purpose, String entryKey, String lootKey) {
      this.target = target;
      this.purpose = purpose;
      this.entryKey = entryKey;
      this.lootKey = lootKey;
   }

   public Inventory getInventory() {
      return null;
   }

   @Generated
   public LootEditorTarget getTarget() {
      return this.target;
   }

   @Generated
   public LootCaptureHolder.Purpose getPurpose() {
      return this.purpose;
   }

   @Generated
   public String getEntryKey() {
      return this.entryKey;
   }

   @Generated
   public String getLootKey() {
      return this.lootKey;
   }

   public static enum Purpose {
      ADD_LOOT,
      REPLACE_LOOT;
   }
}
