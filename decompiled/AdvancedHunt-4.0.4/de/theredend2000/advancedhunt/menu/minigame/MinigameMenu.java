package de.theredend2000.advancedhunt.menu.minigame;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.menu.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public abstract class MinigameMenu extends Menu {
   protected final List<BukkitTask> activeTasks = new ArrayList<>();
   protected final Consumer<Boolean> onFinish;
   protected boolean finished = false;

   public MinigameMenu(Player player, Main plugin, Consumer<Boolean> onFinish) {
      super(player, plugin);
      this.onFinish = onFinish;
   }

   protected void finish(boolean success) {
      if (!this.finished) {
         this.finished = true;
         this.cancelAllTasks();
         this.playerMenuUtility.closeInventory();
         if (this.onFinish != null) {
            this.onFinish.accept(success);
         }
      }
   }

   @Override
   public void onClose(InventoryCloseEvent event) {
      this.cancelAllTasks();
      if (!this.finished && this.onFinish != null) {
         this.onFinish.accept(false);
      }
   }

   protected BukkitTask scheduleTask(final Runnable action, long delay) {
      BukkitTask task = (new BukkitRunnable() {
         public void run() {
            action.run();
         }
      }).runTaskLater(this.plugin, delay);
      this.activeTasks.add(task);
      return task;
   }

   protected BukkitTask scheduleTaskTimer(final Runnable action, long delay, long period) {
      BukkitTask task = (new BukkitRunnable() {
         public void run() {
            action.run();
         }
      }).runTaskTimer(this.plugin, delay, period);
      this.activeTasks.add(task);
      return task;
   }

   protected void cancelAllTasks() {
      for (BukkitTask task : this.activeTasks) {
         if (task != null && !task.isCancelled()) {
            task.cancel();
         }
      }

      this.activeTasks.clear();
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void addCloseOrBack() {
   }
}
