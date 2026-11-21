package vrx.economy.vRXClans;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ClanGUIHolder implements InventoryHolder {
   private Inventory inventory;
   private final String menuType;
   private final String targetData;

   public ClanGUIHolder(String menuType) {
      this.menuType = menuType;
      this.targetData = "";
   }

   public ClanGUIHolder(String menuType, String targetData) {
      this.menuType = menuType;
      this.targetData = targetData;
   }

   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public String getMenuType() {
      return this.menuType;
   }

   public String getTargetData() {
      return this.targetData;
   }
}
