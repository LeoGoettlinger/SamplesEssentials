package com.lishid.openinv.command;

import com.lishid.openinv.util.SearchHelper;
import com.lishid.openinv.util.TabCompleter;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.lang.Replacement;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class SearchInvCommand implements TabExecutor {
   @NotNull
   private final LanguageManager lang;

   public SearchInvCommand(@NotNull LanguageManager lang) {
      this.lang = lang;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      Material material = null;
      if (args.length >= 1) {
         material = Material.matchMaterial(args[0]);
      }

      if (material == null) {
         this.lang.sendMessage(sender, "messages.error.invalidMaterial", new Replacement("%target%", args.length > 0 ? args[0] : "null"));
         return false;
      } else {
         int count = 1;
         if (args.length >= 2) {
            try {
               count = Integer.parseInt(args[1]);
            } catch (NumberFormatException var12) {
               this.lang.sendMessage(sender, "messages.error.invalidNumber", new Replacement("%target%", args[1]));
               return false;
            }
         }

         StringBuilder players = new StringBuilder();
         boolean searchInv = command.getName().equals("searchinv");

         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Inventory inventory = (Inventory)(searchInv ? player.getInventory() : player.getEnderChest());
            if (this.findMatch(inventory, material, count)) {
               players.append(player.getName()).append(", ");
               break;
            }
         }

         if (!players.isEmpty()) {
            players.delete(players.length() - 2, players.length());
            this.lang
               .sendMessage(
                  sender, "messages.info.player.matches", new Replacement("%target%", material.name()), new Replacement("%detail%", players.toString())
               );
            return true;
         } else {
            this.lang.sendMessage(sender, "messages.info.player.noMatches", new Replacement("%target%", material.name()));
            return true;
         }
      }
   }

   private boolean findMatch(@NotNull Inventory inventory, @NotNull Material material, int count) {
      AtomicInteger total = new AtomicInteger();
      return SearchHelper.findMatch(
         inventory, itemStack -> itemStack.getType() == material && itemStack.getAmount() > 0 ? total.addAndGet(itemStack.getAmount()) >= count : false
      );
   }

   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (args.length >= 1 && args.length <= 2 && command.testPermissionSilent(sender)) {
         String argument = args[args.length - 1];
         return args.length == 1 ? TabCompleter.completeEnum(argument, Material.class) : TabCompleter.completeInteger(argument);
      } else {
         return Collections.emptyList();
      }
   }
}
