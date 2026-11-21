package org.lushplugins.lushrewards.libraries.lushlib.gui.inventory;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.LushLib;
import org.lushplugins.lushrewards.libraries.lushlib.gui.button.Button;
import org.lushplugins.lushrewards.libraries.lushlib.gui.button.ItemButton;
import org.lushplugins.lushrewards.libraries.lushlib.gui.button.SimpleItemButton;
import org.lushplugins.lushrewards.libraries.lushlib.manager.GuiManager;
import org.lushplugins.lushrewards.libraries.lushlib.utils.DisplayItemStack;

public abstract class Gui {
   private final Inventory inventory;
   private final Player player;
   private final ConcurrentHashMap<Integer, Button> buttons = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Integer, Boolean> slotLockMap = new ConcurrentHashMap<>();

   public Gui(int size, String title, Player player) {
      this.inventory = Bukkit.getServer().createInventory(null, size, title);
      this.player = player;
   }

   public Gui(InventoryType inventoryType, String title, Player player) {
      this.inventory = Bukkit.getServer().createInventory(null, inventoryType, title);
      this.player = player;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public Player getPlayer() {
      return this.player;
   }

   @Deprecated(
      since = "0.7.0",
      forRemoval = true
   )
   protected void addItem(int slot, @Nullable ItemStack item) {
      this.setItem(slot, item);
   }

   protected void setItem(int slot, @Nullable ItemStack item) {
      if (item == null) {
         item = new ItemStack(Material.AIR);
      }

      this.inventory.setItem(slot, item);
   }

   @Nullable
   public Button getButton(int slot) {
      return this.buttons.get(slot);
   }

   public ImmutableMap<Integer, Button> getButtons() {
      return ImmutableMap.copyOf(this.buttons);
   }

   public void addButton(int slot, ItemStack item, Consumer<InventoryClickEvent> task) {
      this.addButton(slot, new SimpleItemButton(DisplayItemStack.builder(item).build(), task));
   }

   public void addButton(int slot, DisplayItemStack item, Consumer<InventoryClickEvent> task) {
      this.addButton(slot, new SimpleItemButton(item, task));
   }

   public void addButton(int slot, Consumer<InventoryClickEvent> task) {
      this.addButton(slot, new SimpleItemButton(null, task));
   }

   public void addButton(int slot, Button button) {
      this.buttons.put(slot, button);
   }

   public void removeButton(int slot) {
      this.buttons.remove(slot);
   }

   public void clearButtons() {
      this.buttons.clear();
   }

   public void lockSlot(int slot) {
      this.lockSlot(slot, true);
   }

   public void unlockSlot(int slot) {
      this.lockSlot(slot, false);
   }

   public void lockSlot(int slot, boolean locked) {
      this.slotLockMap.put(slot, locked);
   }

   public void lockSlots(int... slots) {
      for (int slot : slots) {
         this.lockSlot(slot, true);
      }
   }

   public void unlockSlots(int... slots) {
      for (int slot : slots) {
         this.lockSlot(slot, false);
      }
   }

   public boolean isSlotLocked(int slot) {
      return this.slotLockMap.getOrDefault(slot, true);
   }

   public void refresh(int slot) {
      Button button = this.buttons.get(slot);
      if (button instanceof ItemButton itemButton) {
         this.setItem(slot, itemButton.getItemStack(this.player));
      }
   }

   public void refresh() {
      this.buttons.forEach((slot, button) -> {
         if (button instanceof ItemButton itemButton) {
            this.setItem(slot, itemButton.getItemStack(this.player));
         }
      });
   }

   @Deprecated(
      since = "0.7.2"
   )
   public void recalculateContents() {
   }

   public void open() {
      this.refresh();
      if (!Bukkit.isPrimaryThread()) {
         Bukkit.getScheduler().runTask(LushLib.getInstance().getPlugin(), () -> this.player.openInventory(this.inventory));
      } else {
         this.player.openInventory(this.inventory);
      }

      LushLib.getInstance().getPlugin().getManager(GuiManager.class).ifPresent(guiManager -> guiManager.addGui(this.player.getUniqueId(), this));
   }

   public void close() {
      this.player.closeInventory();
   }

   public void onOpen(InventoryOpenEvent event) {
   }

   public void onClose(InventoryCloseEvent event) {
      LushLib.getInstance().getPlugin().getManager(GuiManager.class).ifPresent(guiManager -> guiManager.removeGui(this.player.getUniqueId()));
   }

   public void onClick(InventoryClickEvent event) {
      this.onClick(event, false);
   }

   public void onClick(InventoryClickEvent event, boolean cancelAll) {
      Inventory clickedInventory = event.getClickedInventory();
      if (clickedInventory != null) {
         int slot = event.getRawSlot();
         Button button = this.buttons.get(slot);
         if (button != null) {
            try {
               button.click(event);
            } catch (Throwable var15) {
               var15.printStackTrace();
            }
         }

         if (cancelAll) {
            event.setCancelled(true);
         } else {
            switch (event.getAction()) {
               case COLLECT_TO_CURSOR:
                  event.setCancelled(true);
                  break;
               case DROP_ALL_SLOT:
               case DROP_ONE_SLOT:
               case PLACE_ALL:
               case PLACE_SOME:
               case PLACE_ONE:
               case PICKUP_ALL:
               case PICKUP_HALF:
               case PICKUP_SOME:
               case PICKUP_ONE:
               case SWAP_WITH_CURSOR:
               case CLONE_STACK:
               case HOTBAR_SWAP:
               case HOTBAR_MOVE_AND_READD:
                  if (clickedInventory.equals(this.inventory) && this.isSlotLocked(slot)) {
                     event.setCancelled(true);
                  }
                  break;
               case MOVE_TO_OTHER_INVENTORY:
                  event.setCancelled(true);
                  if (!clickedInventory.equals(this.inventory)) {
                     List<Integer> unlockedSlots = this.slotLockMap.entrySet().stream().filter(entry -> !entry.getValue()).map(Entry::getKey).sorted().toList();
                     ItemStack clickedItem = event.getCurrentItem();
                     if (clickedItem != null) {
                        int remainingToDistribute = clickedItem.getAmount();
                        int backupDestinationSlot = -1;
                        boolean complete = false;

                        for (int unlockedSlot : unlockedSlots) {
                           if (complete) {
                              break;
                           }

                           ItemStack slotItem = this.inventory.getItem(unlockedSlot);
                           if ((slotItem == null || slotItem.getType() == Material.AIR) && backupDestinationSlot == -1) {
                              backupDestinationSlot = unlockedSlot;
                           } else if (slotItem != null && slotItem.isSimilar(clickedItem)) {
                              int spaceInStack = slotItem.getMaxStackSize() - slotItem.getAmount();
                              if (spaceInStack <= remainingToDistribute) {
                                 slotItem.setAmount(slotItem.getAmount() + remainingToDistribute);
                                 clickedItem.setType(Material.AIR);
                                 complete = true;
                              } else if (spaceInStack > 0) {
                                 remainingToDistribute -= spaceInStack;
                                 slotItem.setAmount(slotItem.getMaxStackSize());
                                 clickedItem.setAmount(clickedItem.getAmount() - spaceInStack);
                              }
                           }
                        }

                        if (!complete && backupDestinationSlot != -1) {
                           this.inventory.setItem(backupDestinationSlot, clickedItem);
                           event.getInventory().setItem(event.getSlot(), null);
                        }
                     }
                  }
            }
         }
      }
   }

   public void onDrag(InventoryDragEvent event) {
      for (int slot : event.getRawSlots()) {
         if (slot <= 53 && this.isSlotLocked(slot)) {
            event.setCancelled(true);
            return;
         }
      }
   }

   static {
      LushLib.getInstance().getPlugin().registerManager(new GuiManager());
   }
}
