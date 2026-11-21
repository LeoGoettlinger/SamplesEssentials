package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShareItem {
   public static void showOnChat(Player player) {
      if (!player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
         ItemStack itemStack = player.getInventory().getItemInMainHand();
         if (itemStack.hasItemMeta()) {
            if (itemStack.getItemMeta().hasDisplayName()) {
               if (itemStack.getItemMeta().hasLore()) {
                  String name = itemStack.getItemMeta().getDisplayName();
                  TextComponent interactiveMessage = new TextComponent(player.getDisplayName() + ": " + name);
                  setItemHoverEvent(interactiveMessage, itemStack);

                  for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                     onlinePlayer.spigot().sendMessage(interactiveMessage);
                  }
               }
            }
         }
      }
   }

   public static void setItemHoverEvent(TextComponent textComponent, ItemStack itemStack) {
      if (EliteItemManager.isEliteMobsItem(itemStack)) {
         String stringList = itemStack.getItemMeta().getDisplayName();
         if (itemStack.getItemMeta().hasLore()) {
            for (String loreString : itemStack.getItemMeta().getLore()) {
               stringList = stringList + "\n" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + loreString;
            }
         }

         textComponent.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(stringList).create()));
      }
   }

   public static TextComponent hoverableItemTextComponent(ItemStack itemStack) {
      TextComponent textComponent = new TextComponent();
      String stringList = "";
      if (itemStack.getItemMeta().hasLore()) {
         for (String loreString : itemStack.getItemMeta().getLore()) {
            stringList = stringList + "\n" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + loreString;
         }
      }

      textComponent.setText(itemStack.getItemMeta().getDisplayName());
      textComponent.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(stringList).create()));
      return textComponent;
   }
}
