package net.coreprotect.utility;

import org.bukkit.ChatColor;

public final class Color {
   public static final String BLACK = ChatColor.BLACK.toString();
   public static final String DARK_BLUE = ChatColor.DARK_BLUE.toString();
   public static final String DARK_GREEN = ChatColor.DARK_GREEN.toString();
   public static String DARK_AQUA = ChatColor.DARK_AQUA.toString();
   public static final String DARK_RED = ChatColor.DARK_RED.toString();
   public static final String DARK_PURPLE = ChatColor.DARK_PURPLE.toString();
   public static final String GOLD = ChatColor.GOLD.toString();
   public static final String GREY = ChatColor.GRAY.toString();
   public static final String DARK_GREY = ChatColor.DARK_GRAY.toString();
   public static final String BLUE = ChatColor.BLUE.toString();
   public static final String GREEN = ChatColor.GREEN.toString();
   public static final String AQUA = ChatColor.AQUA.toString();
   public static final String RED = ChatColor.RED.toString();
   public static final String LIGHT_PURPLE = ChatColor.LIGHT_PURPLE.toString();
   public static final String YELLOW = ChatColor.YELLOW.toString();
   public static final String WHITE = ChatColor.WHITE.toString();
   public static final String MAGIC = "§k";
   public static final String BOLD = "§l";
   public static final String STRIKETHROUGH = "§m";
   public static final String UNDERLINE = "§n";
   public static final String ITALIC = "§o";
   public static final String RESET = "§r";

   private Color() {
      throw new IllegalStateException("Utility class");
   }
}
