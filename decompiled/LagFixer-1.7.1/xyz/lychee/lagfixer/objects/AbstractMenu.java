package xyz.lychee.lagfixer.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.utils.ItemBuilder;

public abstract class AbstractMenu implements Listener {
   private static final ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("&8#").build();
   private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&8#").build();
   private static final ItemStack disabled = ItemBuilder.createSkull(
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTRiZDlhNDViOTY4MWNlYTViMjhjNzBmNzVhNjk1NmIxZjU5NGZlYzg0MGI5NjA3Nzk4ZmIxZTcwNzc2NDQzMCJ9fX0="
      )
      .setName("&c&lDISABLED!")
      .setLore(" &8{*} &7Click to &aenable &7this module!")
      .build();
   private static final ItemStack enabled = ItemBuilder.createSkull(
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTkwNzkzZjU2NjE2ZjEwMTUwMmRlMWQzNGViMjU0NGY2MDdkOTg5MDBlMzY5OTM2OTI5NTMxOWU2MzBkY2Y2ZCJ9fX0="
      )
      .setName("&a&lENABLED!")
      .setLore(" &8{*} &7Click to &cdisable &7this module!")
      .build();
   private static final ItemStack back = ItemBuilder.createSkull(
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjM0OTM5ZDI2NDQ0YTU3MzI3ZjA2NGMzOTI4ZGE2MWYzNmNhZjYyMmRlYmU3NGMzM2Y4ZjhhMzZkYTIyIn19fQ=="
      )
      .setName("&3&lPREVIOUS MENU!")
      .setLore(" &8{*} &7Click to &3return &7to previous menu!")
      .build();
   private final LagFixer plugin;
   private final Inventory inv;
   private final BukkitTask task;
   private final HashMap<Integer, AbstractMenu.ItemClickEvent> clicks = new HashMap<>();
   private boolean updated = false;

   public AbstractMenu(LagFixer plugin, int size, String title, int interval, boolean async) {
      this.plugin = plugin;
      this.inv = Bukkit.createInventory(null, size, title);
      this.task = interval > 0 ? SupportManager.getInstance().getFork().runTimer(async, () -> {
         if (!this.updated || !this.inv.getViewers().isEmpty()) {
            this.updateAll();
         }
      }, 1L, interval, TimeUnit.SECONDS) : null;
   }

   public void updateAll() {
      this.clicks.forEach((slot, event) -> {
         try {
            this.inv.setItem(slot, event.getCallableItem().call());
         } catch (Exception var4) {
         }
      });
      this.update();
      this.updated = true;
   }

   public abstract void update();

   public abstract void handleClick(InventoryClickEvent var1, ItemStack var2);

   public abstract AbstractMenu previousMenu();

   public void load() {
      Bukkit.getPluginManager().registerEvents(this, this.plugin);
   }

   public void unload() {
      new HashSet(this.inv.getViewers()).forEach(HumanEntity::closeInventory);
      this.inv.clear();
      HandlerList.unregisterAll(this);
   }

   public void itemClickEvent(int slot, Callable<ItemStack> item, Consumer<InventoryClickEvent> event) {
      this.clicks.put(slot, new AbstractMenu.ItemClickEvent(item, event));
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void inventoryClickEvent(InventoryClickEvent e) {
      ItemStack click = e.getCurrentItem();
      if (click != null && click.getType() != Material.AIR && Objects.equals(e.getClickedInventory(), this.inv)) {
         e.setCancelled(true);
         AbstractMenu.ItemClickEvent itemClickEvent = this.clicks.get(e.getSlot());
         if (itemClickEvent != null && itemClickEvent.getEventConsumer() != null) {
            itemClickEvent.getEventConsumer().accept(e);
            return;
         }

         if (click.equals(getBack())) {
            AbstractMenu back = this.previousMenu();
            if (back != null) {
               e.getWhoClicked().openInventory(back.getInv());
            }

            return;
         }

         this.handleClick(e, click);
      }
   }

   public void surroundInventory() {
      int size = this.inv.getSize();

      for (int i = 0; i < 9; i++) {
         this.inv.setItem(i, getBorder());
         this.inv.setItem(size - 9 + i, getBorder());
      }

      for (int var3 = 9; var3 < size - 9; var3 += 9) {
         this.inv.setItem(var3, getBorder());
         this.inv.setItem(var3 + 8, getBorder());
      }

      if (this.previousMenu() != null) {
         this.inv.setItem(size - 1, getBack());
      }
   }

   public void fillInventory() {
      for (int i = 0; i < this.inv.getSize(); i++) {
         ItemStack invitem = this.inv.getItem(i);
         if (invitem == null || invitem.getType() == Material.AIR) {
            this.inv.setItem(i, getFiller());
         }
      }
   }

   @Generated
   public LagFixer getPlugin() {
      return this.plugin;
   }

   @Generated
   public Inventory getInv() {
      return this.inv;
   }

   @Generated
   public BukkitTask getTask() {
      return this.task;
   }

   @Generated
   public HashMap<Integer, AbstractMenu.ItemClickEvent> getClicks() {
      return this.clicks;
   }

   @Generated
   public boolean isUpdated() {
      return this.updated;
   }

   @Generated
   public static ItemStack getBorder() {
      return border;
   }

   @Generated
   public static ItemStack getFiller() {
      return filler;
   }

   @Generated
   public static ItemStack getDisabled() {
      return disabled;
   }

   @Generated
   public static ItemStack getEnabled() {
      return enabled;
   }

   @Generated
   public static ItemStack getBack() {
      return back;
   }

   private static class ItemClickEvent {
      private final Callable<ItemStack> callableItem;
      private final Consumer<InventoryClickEvent> eventConsumer;

      @Generated
      public Callable<ItemStack> getCallableItem() {
         return this.callableItem;
      }

      @Generated
      public Consumer<InventoryClickEvent> getEventConsumer() {
         return this.eventConsumer;
      }

      @Generated
      public ItemClickEvent(Callable<ItemStack> callableItem, Consumer<InventoryClickEvent> eventConsumer) {
         this.callableItem = callableItem;
         this.eventConsumer = eventConsumer;
      }
   }
}
