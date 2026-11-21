package net.coreprotect.paper;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public interface PaperInterface {
   InventoryHolder getHolder(Inventory var1, boolean var2);

   boolean isStopping(Server var1);

   String getLine(Sign var1, int var2);

   void teleportAsync(Entity var1, Location var2);

   String getSkullOwner(Skull var1);

   String getSkullSkin(Skull var1);

   void setSkullOwner(Skull var1, String var2);

   void setSkullSkin(Skull var1, String var2);
}
