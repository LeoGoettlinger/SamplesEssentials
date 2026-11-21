package xyz.lychee.lagfixer.utils;

import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.hooks.PlaceholderAPIHook;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.TextComponent;
import xyz.lychee.lagfixer.managers.ConfigManager;
import xyz.lychee.lagfixer.managers.HookManager;

public class MessageUtils {
   private static final Map<String, String> REPLACEMENTS = Map.of("{*}", "•", "{>>}", "»", "{<<}", "«");

   public static String fixColors(@Nullable CommandSender sender, String message) {
      if (message == null) {
         return "";
      } else {
         String processed = applyPapi(sender, applyReplacements(message));
         return ChatColor.translateAlternateColorCodes('&', processed);
      }
   }

   public static TextComponent colors(@Nullable CommandSender sender, String message) {
      if (message != null && !message.isEmpty()) {
         String processed = applyPapi(sender, applyReplacements(message));
         return Language.getSerializer().deserialize(processed);
      } else {
         return Component.empty();
      }
   }

   private static String applyReplacements(String text) {
      for (Entry<String, String> entry : REPLACEMENTS.entrySet()) {
         text = text.replace(entry.getKey(), entry.getValue());
      }

      return text;
   }

   private static String applyPapi(@Nullable CommandSender sender, String text) {
      if (sender instanceof Player player) {
         PlaceholderAPIHook papi = HookManager.getInstance().getHookIfLoaded(PlaceholderAPIHook.class);
         if (papi != null) {
            return papi.applyPlaceholders(player, text);
         }
      }

      return text;
   }

   public static boolean sendMessage(boolean prefix, CommandSender sender, String message) {
      Component text = colors(sender, "&f" + message);
      LagFixer.getInstance()
         .getAudiences()
         .sender(sender)
         .sendMessage(prefix ? ((TextComponent)Component.empty().append(ConfigManager.getInstance().getPrefix())).append(text) : text);
      return false;
   }
}
