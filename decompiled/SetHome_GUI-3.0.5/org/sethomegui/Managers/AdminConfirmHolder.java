package org.sethomegui.Managers;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AdminConfirmHolder implements InventoryHolder {
   private final UUID targetUuid;
   private final String homeName;

   public AdminConfirmHolder(UUID targetUuid, String homeName) {
      this.targetUuid = targetUuid;
      this.homeName = homeName;
   }

   public UUID getTargetUuid() {
      return this.targetUuid;
   }

   public String getHomeName() {
      return this.homeName;
   }

   public Inventory getInventory() {
      return null;
   }
}
