package org.popcraft.chunky.platform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.popcraft.chunky.platform.util.Location;
import org.popcraft.chunky.util.Translator;

public class BukkitSender implements Sender {
   private static final Pattern RGB_PATTERN = Pattern.compile("&#[0-9a-fA-F]{6}");
   private static final boolean RGB_COLORS_SUPPORTED;
   private final CommandSender sender;

   public BukkitSender(CommandSender sender) {
      this.sender = sender;
   }

   @Override
   public boolean isPlayer() {
      return this.sender instanceof org.bukkit.entity.Player;
   }

   @Override
   public String getName() {
      return this.sender.getName();
   }

   @Override
   public World getWorld() {
      return new BukkitWorld((org.bukkit.World)Bukkit.getWorlds().get(0));
   }

   @Override
   public Location getLocation() {
      return new Location(this.getWorld(), 0.0, 0.0, 0.0, 0.0F, 0.0F);
   }

   @Override
   public boolean hasPermission(String permission) {
      return this.sender.hasPermission(permission);
   }

   @Override
   public void sendMessage(String key, boolean prefixed, Object... args) {
      this.sender.sendMessage(this.formatColored(Translator.translateKey(key, prefixed, args)));
   }

   protected String formatColored(String message) {
      String coloredMessage = message;
      if (RGB_COLORS_SUPPORTED) {
         for (Matcher rgbMatcher = RGB_PATTERN.matcher(message); rgbMatcher.find(); rgbMatcher = RGB_PATTERN.matcher(coloredMessage)) {
            ChatColor rgbColor = ChatColor.of(rgbMatcher.group().substring(1));
            String messageStart = coloredMessage.substring(0, rgbMatcher.start());
            String messageEnd = coloredMessage.substring(rgbMatcher.end());
            coloredMessage = messageStart + rgbColor + messageEnd;
         }
      }

      return org.bukkit.ChatColor.translateAlternateColorCodes('&', coloredMessage);
   }

   static {
      boolean rgbSupported;
      try {
         Class.forName("net.md_5.bungee.api.ChatColor");
         ChatColor.class.getMethod("of", String.class);
         rgbSupported = true;
      } catch (NoSuchMethodException | ClassNotFoundException var2) {
         rgbSupported = false;
      }

      RGB_COLORS_SUPPORTED = rgbSupported;
   }
}
