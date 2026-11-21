package com.samplecraft.darkdrop.gui;

import com.samplecraft.darkdrop.DarkDrop;
import com.samplecraft.darkdrop.util.MessageUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandInputGUI implements InventoryHolder {
   public static final int SIZE = 54;
   public static final int FIRST_CMD_SLOT = 0;
   public static final int LAST_CMD_SLOT = 44;
   public static final int SLOT_INFO = 45;
   public static final int SLOT_TOGGLE_USE = 46;
   public static final int SLOT_ADD = 47;
   public static final int SLOT_SAVE_RETURN = 52;
   public static final int SLOT_CANCEL = 53;
   public static final int MAX_COMMANDS = 45;
   private final DarkDrop plugin;
   private final SettingsGUI returnTo;
   private Inventory inventory;

   public CommandInputGUI(DarkDrop plugin, SettingsGUI returnTo) {
      this.plugin = plugin;
      this.returnTo = returnTo;
   }

   public void open() {
      Component title = MessageUtil.toComponent(this.plugin.getConfigManager().raw("menu-commands-title"));
      this.inventory = Bukkit.createInventory(this, 54, title);
      this.renderAll();
      Player player = this.returnTo.getPlayer();
      if (player != null) {
         player.openInventory(this.inventory);
      }
   }

   public void refresh() {
      if (this.inventory != null) {
         this.renderAll();
         Player player = this.returnTo.getPlayer();
         if (player != null) {
            player.updateInventory();
         }
      }
   }

   private void renderAll() {
      this.inventory.clear();
      List<String> commands = this.returnTo.getSettings().getGoodLootCommands();

      for (int i = 0; i < commands.size() && i < 45; i++) {
         this.inventory.setItem(0 + i, this.commandItem(i, commands.get(i)));
      }

      this.inventory.setItem(45, this.infoItem(commands.size()));
      this.inventory.setItem(46, this.toggleItem(this.returnTo.getSettings().isUseCommandsAsGoodLoot()));
      this.inventory.setItem(47, this.addItem());
      this.inventory.setItem(52, this.saveItem());
      this.inventory.setItem(53, this.cancelItem());
      ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta meta = separator.getItemMeta();
      meta.displayName(MessageUtil.toComponent(" "));
      separator.setItemMeta(meta);

      for (int i = 48; i < 52; i++) {
         this.inventory.setItem(i, separator);
      }
   }

   private ItemStack commandItem(int index, String cmd) {
      ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent("&eCommand #" + (index + 1)));
      List<Component> lore = new ArrayList<>();
      String display = cmd.length() > 50 ? cmd.substring(0, 50) + "..." : cmd;
      lore.add(MessageUtil.toComponent("&7" + display));
      lore.add(MessageUtil.toComponent(""));
      lore.add(MessageUtil.toComponent("&aLinksklick: &7Bearbeiten (Chat)"));
      lore.add(MessageUtil.toComponent("&cRechtsklick: &7Loeschen"));
      lore.add(MessageUtil.toComponent("&7Shift-Rechtsklick: &7Nach oben/unten"));
      meta.lore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack infoItem(int count) {
      ItemStack item = new ItemStack(Material.BOOK);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent("&aCommands als guter Loot"));
      List<Component> lore = new ArrayList<>();
      lore.add(MessageUtil.toComponent("&7Aktuelle Commands: &e" + count));
      lore.add(MessageUtil.toComponent("&7Maximal: &e45"));
      lore.add(MessageUtil.toComponent(""));
      lore.add(MessageUtil.toComponent("&7Platzhalter:"));
      lore.add(MessageUtil.toComponent("&f{player} &8- &7Spielername"));
      lore.add(MessageUtil.toComponent("&f{x}/{y}/{z} &8- &7Koordinaten"));
      lore.add(MessageUtil.toComponent("&f{world} &8- &7Weltname"));
      lore.add(MessageUtil.toComponent("&f{amount} &8- &7gute Loot-Menge"));
      lore.add(MessageUtil.toComponent(""));
      lore.add(MessageUtil.toComponent("&7Beispiel:"));
      lore.add(MessageUtil.toComponent("&fgive {player} diamond {amount}"));
      meta.lore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack toggleItem(boolean enabled) {
      ItemStack item = new ItemStack(enabled ? Material.LIME_CONCRETE : Material.RED_CONCRETE);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent((enabled ? "&a" : "&c") + "Commands als guten Loot: " + (enabled ? "AN" : "AUS")));
      List<Component> lore = new ArrayList<>();
      lore.add(MessageUtil.toComponent("&7Klick zum Umschalten."));
      if (enabled) {
         lore.add(MessageUtil.toComponent("&7Wenn AN: Bei Erfolg werden die"));
         lore.add(MessageUtil.toComponent("&7Commands zusaetzlich/alternative"));
         lore.add(MessageUtil.toComponent("&7zu Items ausgefuehrt."));
      } else {
         lore.add(MessageUtil.toComponent("&7Wenn AUS: Nur Items werden gedroppt."));
      }

      meta.lore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack addItem() {
      ItemStack item = new ItemStack(Material.EMERALD);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent("&aCommand hinzufuegen"));
      List<Component> lore = new ArrayList<>();
      lore.add(MessageUtil.toComponent("&7Oeffnet einen Chat-Prompt."));
      lore.add(MessageUtil.toComponent("&7Tippe den Command im Chat ein."));
      lore.add(MessageUtil.toComponent("&7Abbruch mit &c'cancel'&7."));
      meta.lore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack saveItem() {
      ItemStack item = new ItemStack(Material.LIME_CONCRETE);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent("&aSpeichern & Zurueck"));
      List<Component> lore = new ArrayList<>();
      lore.add(MessageUtil.toComponent("&7Uebernimmt die Commands und"));
      lore.add(MessageUtil.toComponent("&7kehrt zum Hauptmenue zurueck."));
      meta.lore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack cancelItem() {
      ItemStack item = new ItemStack(Material.BARRIER);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent("&cAbbrechen"));
      List<Component> lore = new ArrayList<>();
      lore.add(MessageUtil.toComponent("&7Verwirft Aenderungen und kehrt"));
      lore.add(MessageUtil.toComponent("&7zum Hauptmenue zurueck."));
      meta.lore(lore);
      item.setItemMeta(meta);
      return item;
   }

   public void handleClick(int slot, boolean right, boolean shift) {
      List<String> commands = this.returnTo.getSettings().getGoodLootCommands();
      if (slot >= 0 && slot <= 44) {
         int idx = slot - 0;
         if (idx < commands.size()) {
            if (right) {
               if (shift) {
                  if (idx > 0) {
                     Collections.swap(commands, idx, idx - 1);
                  }
               } else {
                  commands.remove(idx);
               }
            } else {
               if (!shift) {
                  this.startChatEdit(idx);
                  return;
               }

               if (idx < commands.size() - 1) {
                  Collections.swap(commands, idx, idx + 1);
               }
            }
         }

         this.refresh();
      } else {
         switch (slot) {
            case 46:
               this.returnTo.getSettings().setUseCommandsAsGoodLoot(!this.returnTo.getSettings().isUseCommandsAsGoodLoot());
               this.refresh();
               break;
            case 47:
               if (commands.size() >= 45) {
                  Player p = this.returnTo.getPlayer();
                  if (p != null) {
                     p.sendMessage(this.plugin.getConfigManager().component("commands-max-reached"));
                  }

                  return;
               }

               this.startChatAdd();
            case 48:
            case 49:
            case 50:
            case 51:
            default:
               break;
            case 52:
               this.saveAndReturn();
               break;
            case 53:
               this.cancel();
         }
      }
   }

   private void startChatAdd() {
      Player player = this.returnTo.getPlayer();
      if (player != null) {
         player.closeInventory();
         this.plugin.getCommandInputSession().start(player, null, this);
         player.sendMessage(this.plugin.getConfigManager().component("commands-prompt-add"));
      }
   }

   private void startChatEdit(int index) {
      Player player = this.returnTo.getPlayer();
      if (player != null) {
         String oldCmd = this.returnTo.getSettings().getGoodLootCommands().get(index);
         player.closeInventory();
         this.plugin.getCommandInputSession().start(player, index, this);
         player.sendMessage(this.plugin.getConfigManager().component("commands-prompt-edit", "old", oldCmd));
      }
   }

   public void applyCommandFromChat(Integer index, String command) {
      List<String> commands = this.returnTo.getSettings().getGoodLootCommands();
      if (index == null) {
         if (commands.size() >= 45) {
            Player p = this.returnTo.getPlayer();
            if (p != null) {
               p.sendMessage(this.plugin.getConfigManager().component("commands-max-reached"));
            }

            return;
         }

         commands.add(command);
      } else if (index >= 0 && index < commands.size()) {
         commands.set(index, command);
      }

      this.open();
   }

   public void saveAndReturn() {
      Player player = this.returnTo.getPlayer();
      if (player != null) {
         player.sendMessage(this.plugin.getConfigManager().component("commands-saved", "count", this.returnTo.getSettings().getGoodLootCommands().size()));
      }

      this.returnTo.open();
   }

   public void cancel() {
      this.returnTo.open();
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public SettingsGUI getReturnTo() {
      return this.returnTo;
   }

   public static boolean isButtonSlot(int slot) {
      return slot == 45 || slot == 46 || slot == 47 || slot == 52 || slot == 53;
   }

   public static boolean isCommandSlot(int slot) {
      return slot >= 0 && slot <= 44;
   }
}
