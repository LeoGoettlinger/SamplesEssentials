package com.artillexstudios.axtrade.libs.axapi.gui.inventory.renderer.strategy;

import com.artillexstudios.axtrade.libs.axapi.context.HashMapContext;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.BakedGuiItem;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.Gui;
import com.artillexstudios.axtrade.libs.axapi.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class WaitingRenderStrategy implements RenderStrategy {
   @Override
   public void render(Gui gui, HashMapContext context, boolean newInventory, Int2ObjectMap<BakedGuiItem> items, Inventory inventory, Semaphore lock) {
      CompletableFuture<BakedGuiItem>[] futures = new CompletableFuture[gui.getProviders().size()];
      Player player = gui.getRenderer().getPlayer();
      gui.getProviders().forEach((slot, provider) -> futures[slot] = provider.provide(context, gui.getModifiers()));
      CompletableFuture.allOf(futures).whenComplete((v, throwable) -> {
         for (int i = 0; i < futures.length; i++) {
            BakedGuiItem item = futures[i].join();
            BakedGuiItem previous = (BakedGuiItem)items.put(i, item);
            if (newInventory || previous == null || !previous.stack().equals(item.stack())) {
               inventory.setItem(i, item.stack());
            }
         }

         if (newInventory || gui.getRenderer().isClosed()) {
            gui.getRenderer().setClosed(false);
            Scheduler.get().runAt(player.getLocation(), () -> player.openInventory(inventory));
         }

         lock.release();
      });
   }
}
