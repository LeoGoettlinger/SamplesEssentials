package de.theredend2000.advancedhunt.menu.common;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public abstract class SingleItemInputMenu extends Menu {
   protected static final int ITEM_SLOT = 22;
   private final Menu returnMenu;
   private final boolean consumeItemOnConfirm;
   private boolean confirmed = false;

   protected SingleItemInputMenu(Player player, Main plugin, Menu returnMenu, boolean consumeItemOnConfirm) {
      super(player, plugin);
      this.returnMenu = returnMenu;
      this.consumeItemOnConfirm = consumeItemOnConfirm;
      if (returnMenu != null) {
         this.setPreviousMenu(returnMenu);
      }
   }

   protected abstract String getGuiKeyBase();

   protected abstract String getNoItemErrorKey();

   protected boolean isValidItem(ItemStack item) {
      return item != null && item.getType() != XMaterial.AIR.get();
   }

   protected String getInvalidItemErrorKey() {
      return null;
   }

   protected abstract void onConfirm(ItemStack var1);

   protected void onCancel() {
      if (this.returnMenu != null) {
         this.returnMenu.open();
      } else {
         this.openPreviousMenu();
      }
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage(this.getGuiKeyBase() + ".title", false);
   }

   @Override
   public int getSlots() {
      return 45;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
      int slot = e.getRawSlot();
      if (slot == 22) {
         e.setCancelled(false);
      }

      if (slot >= this.getSlots()) {
         e.setCancelled(false);
      }
   }

   @Override
   public void handleDrag(InventoryDragEvent event) {
      Set<Integer> rawSlots = event.getRawSlots();
      int topSize = event.getInventory().getSize();

      for (int slot : rawSlots) {
         if (slot < topSize && slot != 22) {
            event.setCancelled(true);
            ((Player)event.getWhoClicked()).updateInventory();
            return;
         }
      }
   }

   @Override
   public void addCloseOrBack() {
   }

   @Override
   public void setMenuItems() {
      this.fillBorders(this.FILLER_GLASS);
      ItemStack placeholder = new ItemBuilder(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE)
         .setDisplayName(this.plugin.getMessageManager().getMessage(this.getGuiKeyBase() + ".placeholder.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList(this.getGuiKeyBase() + ".placeholder.lore", false))
         .build();
      this.addStaticItem(22, placeholder);
      ItemStack info = new ItemBuilder(XMaterial.BOOK)
         .setDisplayName(this.plugin.getMessageManager().getMessage(this.getGuiKeyBase() + ".info.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList(this.getGuiKeyBase() + ".info.lore", false).toArray(new String[0]))
         .build();
      this.addStaticItem(4, info);
      ItemStack confirm = new ItemBuilder(XMaterial.LIME_CONCRETE)
         .setDisplayName(this.plugin.getMessageManager().getMessage(this.getGuiKeyBase() + ".confirm.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList(this.getGuiKeyBase() + ".confirm.lore", false).toArray(new String[0]))
         .build();
      this.addButton(39, confirm, e -> this.confirmClicked());
      ItemStack cancel = new ItemBuilder(XMaterial.RED_CONCRETE)
         .setDisplayName(this.plugin.getMessageManager().getMessage(this.getGuiKeyBase() + ".cancel.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList(this.getGuiKeyBase() + ".cancel.lore", false).toArray(new String[0]))
         .build();
      this.addButton(41, cancel, e -> this.cancelClicked());
      this.fillBackground(this.FILLER_GLASS);
      this.inventory.setItem(22, null);
      this.buttons[22] = null;
   }

   private void confirmClicked() {
      ItemStack item = this.inventory.getItem(22);
      if (item == null || item.getType() == XMaterial.AIR.get()) {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage(this.getNoItemErrorKey()));
      } else if (!this.isValidItem(item)) {
         String invalidKey = this.getInvalidItemErrorKey();
         if (invalidKey != null && !invalidKey.trim().isEmpty()) {
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage(invalidKey));
         }
      } else {
         this.confirmed = true;
         ItemStack snapshot = item.clone();
         if (!this.consumeItemOnConfirm) {
            this.giveItemBack(item);
            this.inventory.setItem(22, null);
         }

         this.onConfirm(snapshot);
      }
   }

   private void cancelClicked() {
      this.confirmed = true;
      ItemStack item = this.inventory.getItem(22);
      if (item != null && item.getType() != XMaterial.AIR.get()) {
         this.giveItemBack(item);
      }

      this.onCancel();
   }

   private void giveItemBack(ItemStack item) {
      if (item != null && item.getType() != XMaterial.AIR.get()) {
         Map<Integer, ItemStack> leftover = this.playerMenuUtility.getInventory().addItem(new ItemStack[]{item});
         if (!leftover.isEmpty()) {
            leftover.values().forEach(i -> this.playerMenuUtility.getWorld().dropItemNaturally(this.playerMenuUtility.getLocation(), i));
         }
      }
   }

   @Override
   public void onClose(InventoryCloseEvent event) {
      if (!this.confirmed) {
         ItemStack item = this.inventory.getItem(22);
         if (item != null && item.getType() != XMaterial.AIR.get()) {
            this.giveItemBack(item);
         }

         if (this.returnMenu != null) {
            Bukkit.getScheduler().runTaskLater(this.plugin, this.returnMenu::open, 1L);
         } else {
            Bukkit.getScheduler().runTaskLater(this.plugin, this::openPreviousMenu, 1L);
         }
      }
   }
}
