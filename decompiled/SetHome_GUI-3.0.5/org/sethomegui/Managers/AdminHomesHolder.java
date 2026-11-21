package org.sethomegui.Managers;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AdminHomesHolder implements InventoryHolder {
   private final UUID auditedPlayerUuid;

   public AdminHomesHolder(UUID auditedPlayerUuid) {
      this.auditedPlayerUuid = auditedPlayerUuid;
   }

   public UUID getAuditedPlayerUuid() {
      return this.auditedPlayerUuid;
   }

   public Inventory getInventory() {
      return null;
   }
}
