package com.lishid.openinv.command;

import com.lishid.openinv.util.SearchHelper;
import com.lishid.openinv.util.TabCompleter;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.lang.Replacement;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SearchEnchantCommand implements TabExecutor {
   @NotNull
   private final LanguageManager lang;

   public SearchEnchantCommand(@NotNull LanguageManager lang) {
      this.lang = lang;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (args.length == 0) {
         return false;
      } else {
         Enchantment enchant = null;
         int level = 0;

         for (String argument : args) {
            try {
               level = Integer.parseInt(argument);
            } catch (NumberFormatException var13) {
               argument = argument.toLowerCase(Locale.ENGLISH);
               NamespacedKey key = NamespacedKey.fromString(argument);
               if (key != null) {
                  Enchantment localEnchant = (Enchantment)Registry.ENCHANTMENT.get(key);
                  if (localEnchant != null) {
                     enchant = localEnchant;
                  }
               }
            }
         }

         if (level == 0 && enchant == null) {
            return false;
         } else {
            StringBuilder players = new StringBuilder();

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
               boolean flagInventory = this.containsEnchantment(player.getInventory(), enchant, level);
               boolean flagEnder = this.containsEnchantment(player.getEnderChest(), enchant, level);
               if (flagInventory || flagEnder) {
                  players.append(player.getName()).append(" (");
                  if (flagInventory) {
                     players.append("inv");
                  }

                  if (flagEnder) {
                     if (flagInventory) {
                        players.append(',');
                     }

                     players.append("ender");
                  }

                  players.append("), ");
               }
            }

            if (!players.isEmpty()) {
               players.delete(players.length() - 2, players.length());
               this.lang
                  .sendMessage(
                     sender,
                     "messages.info.player.matches",
                     new Replacement("%target%", (enchant != null ? enchant.getKey().toString() : "") + " >= " + level),
                     new Replacement("%detail%", players.toString())
                  );
               return true;
            } else {
               this.lang
                  .sendMessage(
                     sender,
                     "messages.info.player.noMatches",
                     new Replacement("%target%", (enchant != null ? enchant.getKey().toString() : "") + " >= " + level)
                  );
               return true;
            }
         }
      }
   }

   private boolean containsEnchantment(Inventory inventory, @Nullable Enchantment enchant, int minLevel) {
      return SearchHelper.findMatch(inventory, itemStack -> {
         if (!itemStack.hasItemMeta()) {
            return false;
         } else {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null && meta.hasEnchants()) {
               if (enchant != null) {
                  return meta.getEnchantLevel(enchant) >= minLevel;
               } else {
                  for (int enchLevel : meta.getEnchants().values()) {
                     if (enchLevel >= minLevel) {
                        return true;
                     }
                  }

                  return false;
               }
            } else {
               return false;
            }
         }
      });
   }

   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!command.testPermissionSilent(sender) || args.length < 1 || args.length > 2) {
         return Collections.emptyList();
      } else {
         return args.length == 1
            ? TabCompleter.completeObject(args[0], enchantment -> enchantment.getKey().toString(), Registry.ENCHANTMENT.stream().toArray(Enchantment[]::new))
            : TabCompleter.completeInteger(args[1]);
      }
   }
}
