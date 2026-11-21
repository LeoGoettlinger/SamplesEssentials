package com.artillexstudios.axafkzone.libs.axapi.gui.inventory.renderer.strategy;

import com.artillexstudios.axafkzone.libs.axapi.context.HashMapContext;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.BakedGuiItem;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.Gui;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.concurrent.Semaphore;
import org.bukkit.inventory.Inventory;

public interface RenderStrategy {
   void render(Gui gui, HashMapContext context, boolean newInventory, Int2ObjectMap<BakedGuiItem> items, Inventory inventory, Semaphore lock);
}
