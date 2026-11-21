package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.utils.Scheduler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class Menu implements InventoryHolder {
   protected final ChunkManager plugin;
   protected final Player player;
   protected final GuiManager guiManager;
   protected Inventory inv;
   protected final Map<Integer, Runnable> actions;
   protected final Map<Integer, Runnable> rightActions;

   public Menu(ChunkManager plugin, Player player, GuiManager guiManager) {
      this.plugin = plugin;
      this.player = player;
      this.guiManager = guiManager;
      this.actions = new ConcurrentHashMap<>();
      this.rightActions = new ConcurrentHashMap<>();
   }

   public abstract void build();

   public abstract String getTitle();

   public abstract int getSize();

   protected void create() {
      this.inv = Bukkit.createInventory(this, this.getSize(), this.getTitle());
   }

   protected void setItem(int slot, ItemStack item, Runnable action) {
      if (this.inv != null && slot >= 0 && slot < this.getSize()) {
         this.inv.setItem(slot, item);
         if (action != null) {
            this.actions.put(slot, action);
         } else {
            this.actions.remove(slot);
         }
      }
   }

   protected void setItem(int slot, ItemStack item, Runnable leftAction, Runnable rightAction) {
      this.setItem(slot, item, leftAction);
      if (rightAction != null) {
         this.rightActions.put(slot, rightAction);
      } else {
         this.rightActions.remove(slot);
      }
   }

   public void open() {
      if (this.inv == null) {
         this.create();
      }

      this.actions.clear();
      this.rightActions.clear();
      this.inv.clear();
      this.build();
      this.player.openInventory(this.inv);
   }

   public void refresh() {
      if (this.inv != null && this.player.isOnline()) {
         this.actions.clear();
         this.rightActions.clear();
         this.inv.clear();
         this.build();
      }
   }

   public void handleClick(int slot) {
      this.handleClick(slot, false);
   }

   public void handleClick(int slot, boolean rightClick) {
      if (rightClick) {
         Runnable action = this.rightActions.get(slot);
         if (action != null) {
            Scheduler.runGlobal(this.plugin, action);
            return;
         }
      }

      Runnable action = this.actions.get(slot);
      if (action != null) {
         Scheduler.runGlobal(this.plugin, action);
      }
   }

   public Inventory getInventory() {
      return this.inv;
   }
}
