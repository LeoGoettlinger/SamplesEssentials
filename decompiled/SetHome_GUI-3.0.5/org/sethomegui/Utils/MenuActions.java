package org.sethomegui.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;

public final class MenuActions {
   private static final String CONSOLE_PREFIX = "[console]";
   private static final String PLAYER_PREFIX = "[player]";

   private MenuActions() {
   }

   public static Section findItemSection(Section menuSection, int clickedSlot) {
      if (menuSection == null) {
         return null;
      } else {
         Section itemsSection = menuSection.getSection("items");
         if (itemsSection == null) {
            return null;
         } else {
            for (Object keyObj : itemsSection.getKeys()) {
               Section itemData = itemsSection.getSection(String.valueOf(keyObj));
               if (itemData != null) {
                  if (itemData.contains("slot") && itemData.getInt("slot") == clickedSlot) {
                     return itemData;
                  }

                  if (itemData.contains("slots")) {
                     List<Integer> slots = itemData.getIntList("slots");
                     if (slots != null && slots.contains(clickedSlot)) {
                        return itemData;
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   public static boolean execute(SetHomeGUI plugin, Player player, Section menuSection, int clickedSlot) {
      Section itemSection = findItemSection(menuSection, clickedSlot);
      if (itemSection == null) {
         return false;
      } else {
         List<String> commands = readCommands(itemSection);
         if (commands.isEmpty()) {
            return false;
         } else {
            if (itemSection.getBoolean("close-inventory", true)) {
               player.closeInventory();
            }

            for (String command : commands) {
               dispatch(plugin, player, command);
            }

            return true;
         }
      }
   }

   private static List<String> readCommands(Section itemSection) {
      List<String> commands = itemSection.getStringList("commands");
      if (commands != null && !commands.isEmpty()) {
         return commands;
      } else {
         String single = itemSection.getString("command", null);
         if (single != null && !single.trim().isEmpty()) {
            List<String> wrapped = new ArrayList<>();
            wrapped.add(single);
            return wrapped;
         } else {
            return new ArrayList<>();
         }
      }
   }

   private static void dispatch(SetHomeGUI plugin, Player player, String raw) {
      if (raw != null) {
         String line = raw.trim();
         if (!line.isEmpty()) {
            boolean asConsole = false;
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.startsWith("[console]")) {
               asConsole = true;
               line = line.substring("[console]".length()).trim();
            } else if (lower.startsWith("[player]")) {
               line = line.substring("[player]".length()).trim();
            }

            if (line.startsWith("/")) {
               line = line.substring(1);
            }

            if (!line.isEmpty()) {
               String command = applyPlaceholders(plugin, player, line);
               if (asConsole) {
                  Bukkit.getGlobalRegionScheduler().run(plugin, task -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
               } else {
                  player.getScheduler().run(plugin, task -> player.performCommand(command), null);
               }
            }
         }
      }
   }

   public static String applyPlaceholders(SetHomeGUI plugin, Player player, String text) {
      if (text == null) {
         return "";
      } else {
         String parsed = text.replace("%player_name%", player.getName()).replace("%player%", player.getName());
         if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            parsed = PlaceholderAPI.setPlaceholders(player, parsed);
         }

         return parsed;
      }
   }
}
