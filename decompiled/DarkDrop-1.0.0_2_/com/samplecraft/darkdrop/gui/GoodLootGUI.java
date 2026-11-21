package com.samplecraft.darkdrop.gui;

import com.samplecraft.darkdrop.DarkDrop;
import com.samplecraft.darkdrop.model.DropSettings;
import com.samplecraft.darkdrop.util.MessageUtil;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GoodLootGUI implements InventoryHolder {
   public static final int SIZE = 54;
   public static final int FIRST_INPUT_SLOT = 0;
   public static final int LAST_INPUT_SLOT = 44;
   public static final int SLOT_INFO = 49;
   public static final int SLOT_SAVE_RETURN = 52;
   public static final int SLOT_CANCEL = 53;
   private final DarkDrop plugin;
   private final SettingsGUI returnTo;
   private Inventory inventory;

   public GoodLootGUI(DarkDrop plugin, SettingsGUI returnTo) {
      this.plugin = plugin;
      this.returnTo = returnTo;
   }

   public void open() {
      Component title = MessageUtil.toComponent(this.plugin.getConfigManager().raw("menu-good-loot-title"));
      this.inventory = Bukkit.createInventory(this, 54, title);
      this.renderStatic();
      DropSettings settings = this.returnTo.getSettings();
      List<ItemStack> items = settings.getGoodLootItems();

      for (int i = 0; i < items.size() && i + 0 <= 44; i++) {
         ItemStack t = items.get(i);
         if (t != null && !t.getType().isAir()) {
            this.inventory.setItem(i + 0, t.clone());
         }
      }

      if (this.returnTo.getPlayer() != null) {
         this.returnTo.getPlayer().openInventory(this.inventory);
      }
   }

   private void renderStatic() {
      this.inventory.setItem(49, this.infoItem());
      this.inventory.setItem(52, this.saveItem());
      this.inventory.setItem(53, this.cancelItem());
      ItemStack separator = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
      ItemMeta meta = separator.getItemMeta();
      meta.displayName(MessageUtil.toComponent(" "));
      separator.setItemMeta(meta);

      for (int i = 45; i < 49; i++) {
         this.inventory.setItem(i, separator);
      }

      for (int i = 50; i < 52; i++) {
         this.inventory.setItem(i, separator);
      }
   }

   private ItemStack infoItem() {
      ItemStack item = new ItemStack(Material.BOOK);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent("&aAnleitung: Gute Loot-Vorlagen"));
      List<Component> lore = new ArrayList<>();
      lore.add(MessageUtil.toComponent("&7Lege hier Items in die oberen"));
      lore.add(MessageUtil.toComponent("&745 Slot-Felder ab. Sie werden"));
      lore.add(MessageUtil.toComponent("&7als &eVorlagen &7fuer guten Loot"));
      lore.add(MessageUtil.toComponent("&7gespeichert und nicht verbraucht."));
      lore.add(MessageUtil.toComponent(""));
      lore.add(MessageUtil.toComponent("&7&oItems, die hier liegen,"));
      lore.add(MessageUtil.toComponent("&7&obleiben als Vorlage erhalten."));
      meta.lore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack saveItem() {
      ItemStack item = new ItemStack(Material.LIME_CONCRETE);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent("&aSpeichern & Zurueck"));
      List<Component> lore = new ArrayList<>();
      lore.add(MessageUtil.toComponent("&7Uebernimmt die Items als Vorlagen"));
      lore.add(MessageUtil.toComponent("&7und kehrt zum Hauptmenue zurueck."));
      meta.lore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack cancelItem() {
      ItemStack item = new ItemStack(Material.BARRIER);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent("&cAbbrechen"));
      List<Component> lore = new ArrayList<>();
      lore.add(MessageUtil.toComponent("&7Verwirft die Eingaben und kehrt"));
      lore.add(MessageUtil.toComponent("&7ohne Speichern zurueck."));
      meta.lore(lore);
      item.setItemMeta(meta);
      return item;
   }

   public void saveAndReturn() {
      DropSettings settings = this.returnTo.getSettings();
      List<ItemStack> newItems = new ArrayList<>();

      for (int i = 0; i <= 44; i++) {
         ItemStack stack = this.inventory.getItem(i);
         if (stack != null && !stack.getType().isAir()) {
            newItems.add(stack.clone());
         }
      }

      settings.setGoodLootItems(newItems);

      for (int ix = 0; ix <= 44; ix++) {
         this.inventory.setItem(ix, null);
      }

      Player player = this.returnTo.getPlayer();
      if (player != null) {
         player.sendMessage(this.plugin.getConfigManager().component("good-loot-saved", "count", newItems.size()));
      }

      this.returnTo.open();
   }

   public void cancel() {
      Player player = this.returnTo.getPlayer();

      for (int i = 0; i <= 44; i++) {
         ItemStack stack = this.inventory.getItem(i);
         if (stack != null && !stack.getType().isAir()) {
            this.inventory.setItem(i, null);
            if (player != null) {
               player.getInventory()
                  .addItem(new ItemStack[]{stack})
                  .values()
                  .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            }
         }
      }

      this.returnTo.open();
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public SettingsGUI getReturnTo() {
      return this.returnTo;
   }

   public static boolean isInputSlot(int slot) {
      return slot >= 0 && slot <= 44;
   }

   public static boolean isButtonSlot(int slot) {
      return slot == 52 || slot == 53 || slot == 49;
   }
}
