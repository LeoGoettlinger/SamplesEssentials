package com.cyux.localinv;

import java.util.List;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryCommands implements CommandExecutor {
   private final LocalInventory plugin;

   public InventoryCommands(LocalInventory plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player p)) {
         return true;
      } else if (label.equalsIgnoreCase("ali")) {
         if (!p.hasPermission("localinv.admin")) {
            p.sendMessage("§cNo permission.");
            return true;
         } else if (args.length == 0) {
            p.sendMessage("§cUsage: /ali <player>");
            return true;
         } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            String path = target.getUniqueId().toString();
            if (!this.plugin.getConfig().contains(path)) {
               p.sendMessage("§c" + args[0] + " has no saved inventory.");
               return true;
            } else {
               this.openAdminGUI(p, target);
               return true;
            }
         }
      } else if (label.equalsIgnoreCase("sli")) {
         if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
            this.saveAndClear(p);
            return true;
         } else {
            if (this.plugin.getConfig().contains(p.getUniqueId().toString())) {
               p.sendMessage("§e§l(!) §cOverwrite current save?");
               TextComponent yes = new TextComponent("§a§l[YES] ");
               yes.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/sli confirm"));
               p.spigot().sendMessage(yes);
            } else {
               this.saveAndClear(p);
            }

            return true;
         }
      } else if (label.equalsIgnoreCase("lli")) {
         String path = p.getUniqueId().toString();
         if (!this.plugin.getConfig().contains(path)) {
            p.sendMessage("§cNo save found.");
            return true;
         } else {
            try {
               p.getInventory().setContents(this.loadItems(path + ".main"));
               p.getInventory().setArmorContents(this.loadItems(path + ".armor"));
               p.getInventory().setItemInOffHand(this.plugin.getConfig().getItemStack(path + ".offhand"));
               this.plugin.getConfig().set(path, null);
               this.plugin.saveConfig();
               p.sendMessage("§aInventory loaded!");
            } catch (Exception var8) {
               p.sendMessage("§cLoad error.");
            }

            return true;
         }
      } else {
         return true;
      }
   }

   private void openAdminGUI(Player admin, OfflinePlayer target) {
      String path = target.getUniqueId().toString();
      Inventory gui = Bukkit.createInventory(null, 54, "§0Storage: " + target.getName());
      ItemStack[] main = this.loadItems(path + ".main");
      ItemStack[] armor = this.loadItems(path + ".armor");
      ItemStack offhand = this.plugin.getConfig().getItemStack(path + ".offhand");
      if (main != null) {
         gui.setContents(main);
      }

      if (armor != null) {
         for (int i = 0; i < armor.length; i++) {
            gui.setItem(45 + i, armor[i]);
         }
      }

      gui.setItem(53, offhand);
      admin.openInventory(gui);
      admin.sendMessage("§7Opening saved items for §e" + target.getName());
   }

   private ItemStack[] loadItems(String path) {
      Object data = this.plugin.getConfig().get(path);
      if (data instanceof List) {
         return ((List)data).toArray(new ItemStack[0]);
      } else {
         return data instanceof ItemStack[] ? (ItemStack[])data : null;
      }
   }

   private void saveAndClear(Player p) {
      String path = p.getUniqueId().toString();
      this.plugin.getConfig().set(path + ".main", p.getInventory().getContents());
      this.plugin.getConfig().set(path + ".armor", p.getInventory().getArmorContents());
      this.plugin.getConfig().set(path + ".offhand", p.getInventory().getItemInOffHand());
      this.plugin.saveConfig();
      p.getInventory().clear();
      p.getInventory().setArmorContents(new ItemStack[4]);
      p.getInventory().setItemInOffHand(null);
      p.sendMessage("§a§lSUCCESS! §7Your inventory was saved.");
   }
}
