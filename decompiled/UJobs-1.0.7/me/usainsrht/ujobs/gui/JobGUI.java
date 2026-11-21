package me.usainsrht.ujobs.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public interface JobGUI extends InventoryHolder {
   void onClick(InventoryClickEvent var1);

   void onBottomClick(InventoryClickEvent var1);

   void onDrag(InventoryDragEvent var1);

   void onOpen(InventoryOpenEvent var1);

   void onClose(InventoryCloseEvent var1);
}
