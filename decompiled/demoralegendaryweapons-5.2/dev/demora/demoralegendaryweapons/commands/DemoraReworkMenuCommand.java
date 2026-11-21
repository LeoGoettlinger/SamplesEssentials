package dev.demora.demoralegendaryweapons.commands;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DemoraReworkMenuCommand implements CommandExecutor {
   private final DemoraLegendaryWeapons plugin;

   public DemoraReworkMenuCommand(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(DemoraLegendaryWeapons.getInstance().getMessageWithPrefix("commands.only-players"));
         return true;
      } else if (!player.isOp()) {
         player.sendMessage(this.plugin.getMessageWithPrefix("no-permission"));
         return true;
      } else {
         this.openReworkMenu(player);
         return true;
      }
   }

   public void openReworkMenu(Player player) {
      String title = this.plugin.getMessage("commands.menu-rework-title");
      Inventory menu = Bukkit.createInventory(null, 9, title);
      menu.setItem(0, this.createMenuItem(this.plugin.getAwakenedDragonKatana().createAwakenedDragonKatana(), "awakened_dragon_katana"));
      menu.setItem(1, this.createMenuItem(this.plugin.getAwakenedWitherSickles().createAwakenedWitherSickle(), "awakened_wither_sickles"));
      menu.setItem(2, this.createMenuItem(this.plugin.getReworkElytra().createReworkElytra(), "rework_elytra"));
      menu.setItem(3, this.createMenuItem(this.plugin.getElementalStaff().createElementalStaff(), "elemental_staff"));
      player.openInventory(menu);
   }

   private ItemStack createMenuItem(ItemStack weapon, String weaponId) {
      return weapon.clone();
   }

   public boolean isReworkMenu(Inventory inventory) {
      String reworkTitle = this.plugin.getMessage("commands.menu-rework-title");
      return inventory.getSize() == 9
         && inventory.getViewers().size() > 0
         && reworkTitle.equals(((HumanEntity)inventory.getViewers().get(0)).getOpenInventory().getTitle());
   }
}
