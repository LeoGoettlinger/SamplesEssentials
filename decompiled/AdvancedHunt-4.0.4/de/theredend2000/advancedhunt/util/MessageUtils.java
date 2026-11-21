package de.theredend2000.advancedhunt.util;

import de.theredend2000.advancedhunt.Main;
import java.lang.reflect.Method;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageUtils {
   private static final Method PLAYER_SEND_ACTION_BAR = findPlayerSendActionBarMethod();

   static String normalizeActionBarMessage(String message) {
      String safe = message == null ? "" : message;
      return ChatColor.translateAlternateColorCodes('&', safe);
   }

   static BaseComponent[] toLegacyActionBarComponents(String message) {
      return toLegacyActionBarComponentsFromNormalized(normalizeActionBarMessage(message));
   }

   public static void sendActionBar(Player player, String message) {
      if (player != null) {
         try {
            Main plugin = (Main)JavaPlugin.getPlugin(Main.class);
            if (!plugin.isEnabled()) {
               return;
            }

            if (!Bukkit.isPrimaryThread()) {
               Bukkit.getScheduler().runTask(plugin, () -> sendActionBar(player, message));
               return;
            }

            if (!player.isOnline()) {
               return;
            }

            String normalized = normalizeActionBarMessage(message);
            if (PLAYER_SEND_ACTION_BAR != null) {
               PLAYER_SEND_ACTION_BAR.invoke(player, normalized);
               return;
            }

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, toLegacyActionBarComponentsFromNormalized(normalized));
         } catch (Throwable var4) {
         }
      }
   }

   private static BaseComponent[] toLegacyActionBarComponentsFromNormalized(String normalizedMessage) {
      return normalizedMessage.isEmpty() ? new BaseComponent[]{new TextComponent("")} : TextComponent.fromLegacyText(normalizedMessage);
   }

   private static Method findPlayerSendActionBarMethod() {
      try {
         return Player.class.getMethod("sendActionBar", String.class);
      } catch (NoSuchMethodException var1) {
         return null;
      }
   }
}
