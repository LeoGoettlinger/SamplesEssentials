package me.chancesd.pvpmanager.sdutils.utils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("&?(#[a-fA-F0-9]{6})");
   private static final boolean USE_PLACEHOLDERAPI = checkForPlaceholderAPI();
   private static final boolean HEX_SUPPORTED = checkForBungeeAPI();
   private static final int CENTER_PX = 154;

   private ChatUtils() {
   }

   private static boolean checkForPlaceholderAPI() {
      return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
   }

   private static boolean checkForBungeeAPI() {
      try {
         ChatColor.of("#FFFFFF");
         return true;
      } catch (NoSuchMethodError | NoClassDefFoundError var1) {
         return false;
      }
   }

   @NotNull
   public static String setPlaceholders(@Nullable Player player, @NotNull String message) {
      if (USE_PLACEHOLDERAPI) {
         try {
            return PlaceholderAPI.setPlaceholders(player, message);
         } catch (NoClassDefFoundError var3) {
            return message;
         }
      } else {
         return message;
      }
   }

   @NotNull
   public static String setPlaceholders(@Nullable Player player, @NotNull String message, @NotNull BiFunction<Player, String, String> fallbackProcessor) {
      if (USE_PLACEHOLDERAPI) {
         try {
            return PlaceholderAPI.setPlaceholders(player, message);
         } catch (NoClassDefFoundError var4) {
            return fallbackProcessor.apply(player, message);
         }
      } else {
         return fallbackProcessor.apply(player, message);
      }
   }

   @NotNull
   public static String colorize(@NotNull String message) {
      return org.bukkit.ChatColor.translateAlternateColorCodes('&', colorizeHex(message));
   }

   @NotNull
   public static String colorizeAndProcess(@Nullable Player player, @NotNull String message) {
      return colorize(setPlaceholders(player, message));
   }

   @NotNull
   public static BaseComponent[] colorizeComponent(@NotNull String message) {
      return TextComponent.fromLegacyText(colorize(message));
   }

   @NotNull
   private static String colorizeHex(@NotNull String message) {
      Matcher matcher = HEX_PATTERN.matcher(message);
      StringBuffer buffer = new StringBuffer();

      while (matcher.find()) {
         String hexColor = matcher.group(1);
         if (!HEX_SUPPORTED) {
            char COLOR_CHAR = 167;
            matcher.appendReplacement(
               buffer,
               "§x§"
                  + hexColor.charAt(0)
                  + "§"
                  + hexColor.charAt(1)
                  + "§"
                  + hexColor.charAt(2)
                  + "§"
                  + hexColor.charAt(3)
                  + "§"
                  + hexColor.charAt(4)
                  + "§"
                  + hexColor.charAt(5)
            );
         } else {
            matcher.appendReplacement(buffer, ChatColor.of(hexColor).toString());
         }
      }

      return matcher.appendTail(buffer).toString();
   }

   public static org.bukkit.ChatColor getLastColor(String string) {
      String lastColors = org.bukkit.ChatColor.getLastColors(string);
      return lastColors.isEmpty() ? null : org.bukkit.ChatColor.getByChar(lastColors.replace("§", ""));
   }

   public static void send(@NotNull CommandSender sender, @NotNull String message) {
      sender.sendMessage(colorize(message));
   }

   public static List<String> getMatchingEntries(String token, List<String> toFilter) {
      return toFilter.stream().filter(s -> s.toLowerCase().contains(token.toLowerCase())).collect(Collectors.toList());
   }

   public static int estimateVisualLength(@NotNull String message) {
      String cleanMessage = org.bukkit.ChatColor.stripColor(colorize(message));
      return cleanMessage.length();
   }

   public static boolean isLineTooLong(@NotNull String message, int maxLength) {
      return estimateVisualLength(message) > maxLength;
   }

   public static boolean isLineTooLong(@NotNull String message) {
      return isLineTooLong(message, 50);
   }

   public static String centerMessage(Player player, String message) {
      if (message == null || message.equals("")) {
         player.sendMessage("");
      }

      message = colorize(message);
      int messagePxSize = 0;
      boolean previousCode = false;
      boolean isBold = false;

      for (char c : message.toCharArray()) {
         if (c == 167) {
            previousCode = true;
         } else if (previousCode) {
            previousCode = false;
            if (c != 'l' && c != 'L') {
               isBold = false;
            } else {
               isBold = true;
            }
         } else {
            DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
            messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
            messagePxSize++;
         }
      }

      int halvedMessageSize = messagePxSize / 2;
      int toCompensate = 154 - halvedMessageSize;
      int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
      int compensated = 0;

      StringBuilder sb;
      for (sb = new StringBuilder(); compensated < toCompensate; compensated += spaceLength) {
         sb.append(" ");
      }

      return sb.toString() + message;
   }
}
