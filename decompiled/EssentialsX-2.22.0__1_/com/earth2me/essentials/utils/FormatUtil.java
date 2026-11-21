package com.earth2me.essentials.utils;

import com.earth2me.essentials.adventure.AdventureUtil;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ess3.api.IUser;
import net.ess3.provider.AbstractChatEvent;
import org.bukkit.ChatColor;
import org.bukkit.Color;

public final class FormatUtil {
   public static final Pattern IPPATTERN = Pattern.compile(
      "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
   );
   private static final Set<ChatColor> COLORS = EnumSet.of(
      ChatColor.BLACK,
      ChatColor.DARK_BLUE,
      ChatColor.DARK_GREEN,
      ChatColor.DARK_AQUA,
      ChatColor.DARK_RED,
      ChatColor.DARK_PURPLE,
      ChatColor.GOLD,
      ChatColor.GRAY,
      ChatColor.DARK_GRAY,
      ChatColor.BLUE,
      ChatColor.GREEN,
      ChatColor.AQUA,
      ChatColor.RED,
      ChatColor.LIGHT_PURPLE,
      ChatColor.YELLOW,
      ChatColor.WHITE
   );
   private static final Set<ChatColor> FORMATS = EnumSet.of(ChatColor.BOLD, ChatColor.STRIKETHROUGH, ChatColor.UNDERLINE, ChatColor.ITALIC, ChatColor.RESET);
   private static final Set<ChatColor> MAGIC = EnumSet.of(ChatColor.MAGIC);
   private static final Pattern STRIP_ALL_PATTERN = Pattern.compile("§+([0-9a-fk-orA-FK-OR])");
   private static final Pattern STRIP_RGB_PATTERN = Pattern.compile("§x((?:§[0-9a-fA-F]){6})");
   private static final Pattern REPLACE_ALL_PATTERN = Pattern.compile("(&)?&([0-9a-fk-orA-FK-OR])");
   private static final Pattern REPLACE_ALL_RGB_PATTERN = Pattern.compile("(&)?&#([0-9a-fA-F]{6})");
   private static final Pattern LOGCOLOR_PATTERN = Pattern.compile("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]");
   private static final Pattern ANSI_CONTROL_PATTERN = Pattern.compile(
      "[\\x1B\\x9B][\\[\\]()#;?]*(?:(?:(?:;[-a-zA-Z\\d/#&.:=?%@~_]+)*|[a-zA-Z\\d]+(?:;[-a-zA-Z\\d/#&.:=?%@~_]*)*)?\\x07|(?:\\d{1,4}(?:;\\d{0,4})*)?[\\dA-PR-TZcf-nq-uy=><~])"
   );
   private static final Pattern PAPER_CONTROL_PATTERN = Pattern.compile("(?i)\u007f[0-9A-FK-ORX]");

   private FormatUtil() {
   }

   public static String stripFormat(final String input) {
      return input == null ? null : ChatColor.stripColor(input);
   }

   public static String stripMiniFormat(final String input) {
      return input == null ? null : AdventureUtil.getAdventureFacet().stripTags(input);
   }

   public static String stripEssentialsFormat(final String input) {
      return input == null ? null : stripColor(stripColor(input, REPLACE_ALL_PATTERN), REPLACE_ALL_RGB_PATTERN);
   }

   public static String stripAnsi(final String input) {
      return input == null ? null : stripColor(input, ANSI_CONTROL_PATTERN);
   }

   public static String stripPaper(final String input) {
      return input == null ? null : stripColor(input, PAPER_CONTROL_PATTERN);
   }

   public static String formatMessage(final IUser user, final String permBase, final String input) {
      if (input == null) {
         return null;
      } else {
         String message = formatString(user, permBase, input);
         if (!user.isAuthorized(permBase + ".url")) {
            message = blockURL(message);
         }

         return message;
      }
   }

   public static String replaceFormat(final String input) {
      return input == null ? null : replaceColor(input, EnumSet.allOf(ChatColor.class), true);
   }

   static String replaceColor(final String input, final Set<ChatColor> supported, final boolean rgb) {
      StringBuffer legacyBuilder = new StringBuffer();
      Matcher legacyMatcher = REPLACE_ALL_PATTERN.matcher(input);

      label58:
      while (legacyMatcher.find()) {
         boolean isEscaped = legacyMatcher.group(1) != null;
         if (!isEscaped) {
            char code = legacyMatcher.group(2).toLowerCase(Locale.ROOT).charAt(0);

            for (ChatColor color : supported) {
               if (color.getChar() == code) {
                  legacyMatcher.appendReplacement(legacyBuilder, "§$2");
                  continue label58;
               }
            }
         }

         legacyMatcher.appendReplacement(legacyBuilder, "&$2");
      }

      legacyMatcher.appendTail(legacyBuilder);
      if (!rgb) {
         return legacyBuilder.toString();
      } else {
         StringBuffer rgbBuilder = new StringBuffer();
         Matcher rgbMatcher = REPLACE_ALL_RGB_PATTERN.matcher(legacyBuilder.toString());

         while (true) {
            while (true) {
               if (!rgbMatcher.find()) {
                  rgbMatcher.appendTail(rgbBuilder);
                  return rgbBuilder.toString();
               }

               boolean isEscaped = rgbMatcher.group(1) != null;
               if (isEscaped) {
                  break;
               }

               try {
                  String hexCode = rgbMatcher.group(2);
                  rgbMatcher.appendReplacement(rgbBuilder, parseHexColor(hexCode));
               } catch (NumberFormatException var9) {
                  break;
               }
            }

            rgbMatcher.appendReplacement(rgbBuilder, "&#$2");
         }
      }
   }

   public static String parseHexColor(String hexColor) throws NumberFormatException {
      if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_16_1_R01)) {
         throw new NumberFormatException("Cannot use RGB colors in versions < 1.16");
      } else {
         if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
         }

         if (hexColor.length() != 6) {
            throw new NumberFormatException("Invalid hex length");
         } else {
            Color.fromRGB(Integer.decode("#" + hexColor));
            StringBuilder assembledColorCode = new StringBuilder();
            assembledColorCode.append("§x");

            for (char curChar : hexColor.toCharArray()) {
               assembledColorCode.append('§').append(curChar);
            }

            return assembledColorCode.toString();
         }
      }
   }

   static String stripColor(final String input, final Set<ChatColor> strip) {
      StringBuffer builder = new StringBuffer();
      Matcher matcher = STRIP_ALL_PATTERN.matcher(input);

      label23:
      while (matcher.find()) {
         char code = matcher.group(1).toLowerCase(Locale.ROOT).charAt(0);

         for (ChatColor color : strip) {
            if (color.getChar() == code) {
               matcher.appendReplacement(builder, "");
               continue label23;
            }
         }

         matcher.appendReplacement(builder, "$0");
      }

      matcher.appendTail(builder);
      return builder.toString();
   }

   public static String unformatString(final String message) {
      return message == null ? null : unformatString(message, EnumSet.allOf(ChatColor.class), true);
   }

   public static String unformatString(final IUser user, final String permBase, final String message) {
      return message == null ? null : unformatString(message, getSupported(user, permBase), user.isAuthorized(permBase + ".rgb"));
   }

   public static String unformatString(String message, final EnumSet<ChatColor> supported, boolean rgb) {
      if (message == null) {
         return null;
      } else {
         StringBuffer rgbBuilder = new StringBuffer();
         Matcher rgbMatcher = STRIP_RGB_PATTERN.matcher(message);

         while (rgbMatcher.find()) {
            String code = rgbMatcher.group(1).replace(String.valueOf('§'), "");
            if (rgb) {
               rgbMatcher.appendReplacement(rgbBuilder, "&#" + code);
            } else {
               rgbMatcher.appendReplacement(rgbBuilder, "");
            }
         }

         rgbMatcher.appendTail(rgbBuilder);
         message = rgbBuilder.toString();
         StringBuffer builder = new StringBuffer();
         Matcher matcher = STRIP_ALL_PATTERN.matcher(message);

         label33:
         while (matcher.find()) {
            char code = matcher.group(1).toLowerCase(Locale.ROOT).charAt(0);

            for (ChatColor color : supported) {
               if (color.getChar() == code) {
                  matcher.appendReplacement(builder, "&" + code);
                  continue label33;
               }
            }

            matcher.appendReplacement(builder, "");
         }

         matcher.appendTail(builder);
         return builder.toString();
      }
   }

   public static String formatString(final IUser user, final String permBase, String message) {
      if (message == null) {
         return null;
      } else {
         EnumSet<ChatColor> supported = getSupported(user, permBase);
         EnumSet<ChatColor> strip = EnumSet.complementOf(supported);
         boolean rgb = user.isAuthorized(permBase + ".rgb");
         if (!strip.isEmpty()) {
            message = stripColor(message, strip);
         }

         if (!supported.isEmpty() || rgb) {
            message = replaceColor(message, supported, rgb);
         }

         return message;
      }
   }

   private static EnumSet<ChatColor> getSupported(final IUser user, final String permBase) {
      EnumSet<ChatColor> supported = EnumSet.noneOf(ChatColor.class);
      if (user.isAuthorized(permBase + ".color")) {
         supported.addAll(COLORS);
      }

      if (user.isAuthorized(permBase + ".format")) {
         supported.addAll(FORMATS);
      }

      if (user.isAuthorized(permBase + ".magic")) {
         supported.addAll(MAGIC);
      }

      for (ChatColor chatColor : ChatColor.values()) {
         String colorName = chatColor.name();
         if (chatColor == ChatColor.MAGIC) {
            colorName = "obfuscated";
         }

         String node = permBase + "." + colorName.toLowerCase(Locale.ROOT);
         if (user.isPermissionSet(node)) {
            if (user.isAuthorized(node)) {
               supported.add(chatColor);
            } else {
               supported.remove(chatColor);
            }
         }
      }

      return supported;
   }

   public static String stripLogColorFormat(final String input) {
      return input == null ? null : stripColor(input, LOGCOLOR_PATTERN);
   }

   static String stripColor(final String input, final Pattern pattern) {
      return pattern.matcher(input).replaceAll("");
   }

   public static String lastCode(final String input) {
      int pos = input.lastIndexOf(167);
      return pos != -1 && pos + 1 != input.length() ? input.substring(pos, pos + 2) : "";
   }

   static String blockURL(final String input) {
      if (input == null) {
         return null;
      } else {
         String text = AbstractChatEvent.URL_PATTERN.matcher(input).replaceAll("$1 $2");

         while (AbstractChatEvent.URL_PATTERN.matcher(text).find()) {
            text = AbstractChatEvent.URL_PATTERN.matcher(text).replaceAll("$1 $2");
         }

         return text;
      }
   }

   public static boolean validIP(final String ipAddress) {
      return IPPATTERN.matcher(ipAddress).matches();
   }
}
