package com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.renderer.strategy;

import com.artillexstudios.axrankmenu.libs.axapi.context.HashMapContext;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.BakedGuiItem;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.Gui;
import com.artillexstudios.axrankmenu.libs.axapi.scheduler.Scheduler;
import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class StreamingRenderStrategy implements RenderStrategy {
   @Override
   public void render(Gui gui, HashMapContext context, boolean newInventory, Int2ObjectMap<BakedGuiItem> items, Inventory inventory, Semaphore lock) {
      CompletableFuture<BakedGuiItem>[] futures = new CompletableFuture[gui.getProviders().size()];
      Player player = gui.getRenderer().getPlayer();
      if (newInventory || gui.getRenderer().isClosed()) {
         gui.getRenderer().setClosed(false);
         Scheduler.get().runAt(player.getLocation(), () -> player.openInventory(inventory));
      }

      gui.getProviders().forEach((slot, provider) -> {
         CompletableFuture<BakedGuiItem> provide = provider.provide(context, gui.getModifiers());
         futures[slot] = provide;
         provide.thenAccept(item -> {
            BakedGuiItem previous = (BakedGuiItem)items.put(slot, item);
            if (newInventory || previous == null || !previous.stack().equals(item.stack())) {
               inventory.setItem(slot, item.stack());
            }
         });
      });
      CompletableFuture.allOf(futures).whenComplete((v, throwable) -> {
         if (throwable != null) {
            LogUtils.error("An exception occurred");
         }

         lock.release();
      });
   }
}
