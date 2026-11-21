package github.nighter.smartspawner.commands.editloot;

import lombok.Generated;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class LootListHolder implements InventoryHolder {
   private final LootEditorTarget target;
   private final String entryKey;

   public LootListHolder(LootEditorTarget target, String entryKey) {
      this.target = target;
      this.entryKey = entryKey;
   }

   public Inventory getInventory() {
      return null;
   }

   @Generated
   public LootEditorTarget getTarget() {
      return this.target;
   }

   @Generated
   public String getEntryKey() {
      return this.entryKey;
   }
}
