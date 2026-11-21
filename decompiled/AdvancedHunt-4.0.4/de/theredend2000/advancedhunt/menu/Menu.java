package de.theredend2000.advancedhunt.menu;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class Menu implements InventoryHolder {
   protected Inventory inventory;
   protected Player playerMenuUtility;
   protected Main plugin;
   protected Button[] buttons;
   protected boolean preventClose = false;
   private volatile boolean closing = false;
   protected ItemStack FILLER_GLASS = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build();
   protected ItemStack EXTRA_GLASS = new ItemBuilder(XMaterial.WHITE_STAINED_GLASS_PANE).hideTooltip(true).build();
   protected final Button FILLER_BUTTON = new Button(this.FILLER_GLASS, null);
   protected Menu previousMenu;

   public Menu(Player playerMenuUtility, Main plugin) {
      this.playerMenuUtility = playerMenuUtility;
      this.plugin = plugin;
   }

   public abstract String getMenuName();

   public abstract int getSlots();

   public abstract void handleMenu(InventoryClickEvent var1);

   public void handleDrag(InventoryDragEvent event) {
      Set<Integer> rawSlots = event.getRawSlots();
      int topSize = event.getInventory().getSize();

      for (int slot : rawSlots) {
         if (slot < topSize) {
            event.setCancelled(true);
            ((Player)event.getWhoClicked()).updateInventory();
            return;
         }
      }
   }

   public abstract void setMenuItems();

   public void open() {
      this.closing = false;
      this.inventory = Bukkit.createInventory(this, this.getSlots(), this.getMenuName());
      this.buttons = new Button[this.getSlots()];
      this.setMenuItems();
      this.addCloseOrBack();
      this.playerMenuUtility.openInventory(this.inventory);
   }

   @NotNull
   public Inventory getInventory() {
      return this.inventory;
   }

   public void setButton(int slot, Button button) {
      if (slot >= 0 && slot < this.buttons.length) {
         this.buttons[slot] = button;
         this.inventory.setItem(slot, button.getIcon());
      }
   }

   public void addButton(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
      this.setButton(slot, new Button(item, action));
   }

   public void addCloseOrBack() {
      ItemStack closeOrBack = this.previousMenu == null ? this.getCloseButton() : this.getBackButton();
      this.addButton(this.getSlots() - 5, closeOrBack, e -> {
         if (this.previousMenu == null) {
            e.getWhoClicked().closeInventory();
         } else {
            this.openPreviousMenu();
         }
      });
   }

   public void addButton(int slot, ItemStack item, Consumer<InventoryClickEvent> action, String permission) {
      this.setButton(slot, new Button(item, e -> {
         if (permission != null && !this.playerMenuUtility.hasPermission(permission)) {
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
         } else {
            if (action != null) {
               action.accept(e);
            }
         }
      }));
   }

   public void addStaticItem(int slot, ItemStack item) {
      if (item.isSimilar(this.FILLER_GLASS)) {
         this.setButton(slot, this.FILLER_BUTTON);
      } else {
         this.setButton(slot, new Button(item, null));
      }
   }

   protected ItemStack buildActionItem(
      Material enabledMaterial, boolean enabled, String nameKey, String loreKeyEnabled, String loreKeyDisabled, String... loreArgsEnabled
   ) {
      Material material = enabled ? enabledMaterial : XMaterial.BARRIER.get();
      String loreKey = enabled ? loreKeyEnabled : loreKeyDisabled;
      ItemBuilder builder = new ItemBuilder(material).setDisplayName(this.plugin.getMessageManager().getMessage(nameKey, false));
      if (enabled && loreArgsEnabled != null && loreArgsEnabled.length > 0) {
         builder.setLore(this.plugin.getMessageManager().getMessageList(loreKey, false, loreArgsEnabled).toArray(new String[0]));
      } else {
         builder.setLore(this.plugin.getMessageManager().getMessageList(loreKey, false).toArray(new String[0]));
      }

      return builder.build();
   }

   public void fillBackground(ItemStack item) {
      Button buttonToUse = item.isSimilar(this.FILLER_GLASS) ? this.FILLER_BUTTON : new Button(item, null);

      for (int i = 0; i < this.getSlots(); i++) {
         if (this.buttons[i] == null) {
            this.setButton(i, buttonToUse);
         }
      }
   }

   public void fillBorders(ItemStack item) {
      int size = this.getSlots();
      int rows = size / 9;
      Button buttonToUse = item.isSimilar(this.FILLER_GLASS) ? this.FILLER_BUTTON : new Button(item, null);

      for (int i = 0; i < 9; i++) {
         if (this.buttons[i] == null) {
            this.setButton(i, buttonToUse);
         }

         if (this.buttons[size - 9 + i] == null) {
            this.setButton(size - 9 + i, buttonToUse);
         }
      }

      for (int i = 1; i < rows - 1; i++) {
         if (this.buttons[i * 9] == null) {
            this.setButton(i * 9, buttonToUse);
         }

         if (this.buttons[i * 9 + 8] == null) {
            this.setButton(i * 9 + 8, buttonToUse);
         }
      }
   }

   public void performClick(InventoryClickEvent event) {
      if (event.getClickedInventory() != null && event.getClickedInventory().equals(this.inventory)) {
         int slot = event.getSlot();
         if (slot >= 0 && slot < this.buttons.length && this.buttons[slot] != null) {
            this.buttons[slot].onClick(event);
         }
      }

      this.handleMenu(event);
   }

   public void onOpen(InventoryOpenEvent event) {
   }

   public void onClose(InventoryCloseEvent event) {
   }

   public boolean isClosing() {
      return this.closing;
   }

   public void markClosing() {
      this.closing = true;
   }

   public Player getPlayer() {
      return this.playerMenuUtility;
   }

   public void refresh() {
      this.inventory.clear();
      this.buttons = new Button[this.getSlots()];
      this.setMenuItems();
      this.addCloseOrBack();
      if (this.playerMenuUtility != null && this.playerMenuUtility.isOnline()) {
         this.playerMenuUtility.updateInventory();
      }
   }

   public void updateSlot(int slot, ItemStack newIcon) {
      if (slot >= 0 && slot < this.buttons.length && this.buttons[slot] != null) {
         this.buttons[slot].setIcon(newIcon);
         this.inventory.setItem(slot, newIcon);
         this.playerMenuUtility.updateInventory();
      }
   }

   public Menu setPreviousMenu(Menu previousMenu) {
      this.previousMenu = previousMenu;
      return this;
   }

   public void openPreviousMenu() {
      if (this.previousMenu != null) {
         this.previousMenu.open();
      } else {
         this.playerMenuUtility.closeInventory();
      }
   }

   public ItemStack getCloseButton() {
      return new ItemBuilder(XMaterial.PLAYER_HEAD)
         .setSkullTexture(
            "ewogICJ0aW1lc3RhbXAiIDogMTc2NzUyNTYyMTI2MywKICAicHJvZmlsZUlkIiA6ICIxMTM0OTAxMTU3ZTE0Yzg0OTE1YTNjMGY3M2RmYzM0NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJab2xlZWV5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2JjMTc1ZjFkMzEyZjNjOTEyZmM1ZjY3MjAyNDNlMmZmMmU1NDk4YjgwMGZjNWI3MDEyNjFhZDM2NTViODJjYWIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="
         )
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.common.close"))
         .build();
   }

   public ItemStack getBackButton() {
      return new ItemBuilder(XMaterial.PLAYER_HEAD)
         .setSkullTexture(
            "ewogICJ0aW1lc3RhbXAiIDogMTc2NzUyMjU3ODMzMCwKICAicHJvZmlsZUlkIiA6ICJmMWFlNzVjYmE3MmU0YWUzYTU0Yzk5ZmYwMWFlZjQ1ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJlcm1zaWVzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q1NjE3Y2UxMjllZDk1YzdiOTE1MTE5YjEwNzQ2ZjM0YzFjYzlkZmYwYWUzZTMzMjM4NjNlYmVkZDAwYmEyOWYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="
         )
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.common.back", "%menu%", this.previousMenu.getMenuName()))
         .build();
   }

   public ItemStack getWarningIcon(String displayName, List<String> lore) {
      return new ItemBuilder(XMaterial.PLAYER_HEAD)
         .setSkullTexture(
            "ewogICJ0aW1lc3RhbXAiIDogMTc2NzUyMjY0NTA3MiwKICAicHJvZmlsZUlkIiA6ICI0OThjYTc2ZGYwODM0NzhmOGY0NjdjOGY1OTQwMjk1MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJHdWx0cm8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZlODRmZmM1NzcxY2VlOWFjMmZiMDMyN2YxMWMyZGRiN2EwOGFjMmYxNjVjNjFjMDUyMWI3YmI5N2VhZGY5ZCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
         )
         .setDisplayName(displayName)
         .setLore(lore)
         .build();
   }
}
