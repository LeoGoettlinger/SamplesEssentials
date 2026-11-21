package github.nighter.smartspawner.commands.near;

import java.util.UUID;
import lombok.Generated;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class NearResultHolder implements InventoryHolder {
   private final UUID playerUUID;
   private final int currentPage;
   private final int totalPages;

   public NearResultHolder(UUID playerUUID, int currentPage, int totalPages) {
      this.playerUUID = playerUUID;
      this.currentPage = currentPage;
      this.totalPages = totalPages;
   }

   public Inventory getInventory() {
      return null;
   }

   @Generated
   public UUID getPlayerUUID() {
      return this.playerUUID;
   }

   @Generated
   public int getCurrentPage() {
      return this.currentPage;
   }

   @Generated
   public int getTotalPages() {
      return this.totalPages;
   }
}
