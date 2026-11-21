package com.magmaguy.elitemobs.magmacore.util;

import com.magmaguy.elitemobs.magmacore.util.minimessage.MiniMessageParser;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ChatColorConverter {
   private static final Pattern HEX_PATTERN = Pattern.compile("&?#([A-Fa-f0-9]{6})");
   private static final Pattern HEX_TAG_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
   private static final Pattern GRADIENT_PATTERN = Pattern.compile("<(?:gradient|g):(#[A-Fa-f0-9]{6}(?::#[A-Fa-f0-9]{6})+)>(.*?)</(?:gradient|g)>");
   private static final Pattern RAINBOW_PATTERN = Pattern.compile("<(?:rainbow|r)(?::(\\d+))?>(.*?)</(?:rainbow|r)>");

   private ChatColorConverter() {
   }

   public static String convert(String string) {
      return string == null ? "" : MiniMessageParser.toLegacy(string);
   }

   public static List<String> convert(List<?> list) {
      if (list == null) {
         return new ArrayList<>();
      } else {
         List<String> convertedList = new ArrayList<>();

         for (Object value : list) {
            convertedList.add(convert(value + ""));
         }

         return convertedList;
      }
   }

   private static String processGradients(String string) {
      Matcher matcher = GRADIENT_PATTERN.matcher(string);
      StringBuffer result = new StringBuffer();

      while (matcher.find()) {
         String colorsStr = matcher.group(1);
         String text = matcher.group(2);
         String[] colorHexes = colorsStr.split(":");
         Color[] colors = new Color[colorHexes.length];

         for (int i = 0; i < colorHexes.length; i++) {
            colors[i] = hexToColor(colorHexes[i]);
         }

         String gradientText = applyGradient(text, colors);
         matcher.appendReplacement(result, Matcher.quoteReplacement(gradientText));
      }

      matcher.appendTail(result);
      return result.toString();
   }

   private static String processRainbow(String string) {
      Matcher matcher = RAINBOW_PATTERN.matcher(string);
      StringBuffer result = new StringBuffer();

      while (matcher.find()) {
         String saturationStr = matcher.group(1);
         String text = matcher.group(2);
         float saturation = 1.0F;
         if (saturationStr != null) {
            saturation = Math.max(0, Math.min(100, Integer.parseInt(saturationStr))) / 100.0F;
         }

         String rainbowText = applyRainbow(text, saturation);
         matcher.appendReplacement(result, Matcher.quoteReplacement(rainbowText));
      }

      matcher.appendTail(result);
      return result.toString();
   }

   private static String processHexTags(String string) {
      Matcher matcher = HEX_TAG_PATTERN.matcher(string);
      StringBuffer result = new StringBuffer();

      while (matcher.find()) {
         String hex = matcher.group(1);
         String replacement = hexToChatColor(hex);
         matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
      }

      matcher.appendTail(result);
      return result.toString();
   }

   private static String processHexColors(String string) {
      Matcher matcher = HEX_PATTERN.matcher(string);
      StringBuffer result = new StringBuffer();

      while (matcher.find()) {
         String hex = matcher.group(1);
         String replacement = hexToChatColor(hex);
         matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
      }

      matcher.appendTail(result);
      return result.toString();
   }

   public static String applyGradient(String text, Color... colors) {
      if (text != null && !text.isEmpty()) {
         if (colors != null && colors.length != 0) {
            if (colors.length == 1) {
               return hexToChatColor(colorToHex(colors[0])) + text;
            } else {
               StringBuilder result = new StringBuilder();
               int length = text.length();
               String strippedText = stripColorCodes(text);
               int strippedLength = strippedText.length();
               if (strippedLength == 0) {
                  return text;
               } else if (strippedLength == 1) {
                  return hexToChatColor(colorToHex(colors[0])) + text;
               } else {
                  int colorIndex = 0;
                  if (colors.length > 1) {
                     int var10000 = strippedLength / (colors.length - 1);
                  }

                  for (int i = 0; i < length; i++) {
                     char c = text.charAt(i);
                     if (c == '&' && i + 1 < length) {
                        char next = text.charAt(i + 1);
                        if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(next) != -1) {
                           result.append(c).append(next);
                           i++;
                           continue;
                        }
                     }

                     if (c == 167 && i + 1 < length) {
                        char next = text.charAt(i + 1);
                        if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(next) != -1) {
                           result.append(c).append(next);
                           i++;
                           continue;
                        }
                     }

                     float position = (float)colorIndex / (strippedLength - 1);
                     float scaledPosition = position * (colors.length - 1);
                     int colorIdx1 = Math.min((int)scaledPosition, colors.length - 1);
                     int colorIdx2 = Math.min(colorIdx1 + 1, colors.length - 1);
                     float localRatio = scaledPosition - colorIdx1;
                     Color interpolated = interpolateColor(colors[colorIdx1], colors[colorIdx2], localRatio);
                     result.append(hexToChatColor(colorToHex(interpolated))).append(c);
                     colorIndex++;
                  }

                  return result.toString();
               }
            }
         } else {
            return text;
         }
      } else {
         return "";
      }
   }

   public static String applyRainbow(String text, float saturation) {
      if (text != null && !text.isEmpty()) {
         StringBuilder result = new StringBuilder();
         String strippedText = stripColorCodes(text);
         int strippedLength = strippedText.length();
         if (strippedLength == 0) {
            return text;
         } else {
            int colorIndex = 0;
            int length = text.length();

            for (int i = 0; i < length; i++) {
               char c = text.charAt(i);
               if (c == '&' && i + 1 < length) {
                  char next = text.charAt(i + 1);
                  if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(next) != -1) {
                     result.append(c).append(next);
                     i++;
                     continue;
                  }
               }

               float hue = (float)colorIndex / strippedLength;
               Color color = Color.getHSBColor(hue, saturation, 1.0F);
               result.append(hexToChatColor(colorToHex(color))).append(c);
               colorIndex++;
            }

            return result.toString();
         }
      } else {
         return "";
      }
   }

   private static String hexToChatColor(String hex) {
      if (hex.startsWith("#")) {
         hex = hex.substring(1);
      }

      try {
         return ChatColor.of("#" + hex).toString();
      } catch (Exception var2) {
         return findNearestLegacyColor(hexToColor("#" + hex)).toString();
      }
   }

   private static Color hexToColor(String hex) {
      if (hex.startsWith("#")) {
         hex = hex.substring(1);
      }

      return new Color(Integer.parseInt(hex.substring(0, 2), 16), Integer.parseInt(hex.substring(2, 4), 16), Integer.parseInt(hex.substring(4, 6), 16));
   }

   private static String colorToHex(Color color) {
      return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
   }

   private static Color interpolateColor(Color color1, Color color2, float ratio) {
      ratio = Math.max(0.0F, Math.min(1.0F, ratio));
      int r = (int)(color1.getRed() + ratio * (color2.getRed() - color1.getRed()));
      int g = (int)(color1.getGreen() + ratio * (color2.getGreen() - color1.getGreen()));
      int b = (int)(color1.getBlue() + ratio * (color2.getBlue() - color1.getBlue()));
      return new Color(r, g, b);
   }

   private static String stripColorCodes(String text) {
      return text == null ? "" : text.replaceAll("&[0-9A-Fa-fK-Ok-oRr]", "").replaceAll("§[0-9A-Fa-fK-Ok-oRr]", "");
   }

   private static org.bukkit.ChatColor findNearestLegacyColor(Color color) {
      org.bukkit.ChatColor nearest = org.bukkit.ChatColor.WHITE;
      double minDistance = Double.MAX_VALUE;
      int[][] legacyColors = new int[][]{
         {0, 0, 0},
         {0, 0, 170},
         {0, 170, 0},
         {0, 170, 170},
         {170, 0, 0},
         {170, 0, 170},
         {255, 170, 0},
         {170, 170, 170},
         {85, 85, 85},
         {85, 85, 255},
         {85, 255, 85},
         {85, 255, 255},
         {255, 85, 85},
         {255, 85, 255},
         {255, 255, 85},
         {255, 255, 255}
      };
      org.bukkit.ChatColor[] colors = new org.bukkit.ChatColor[]{
         org.bukkit.ChatColor.BLACK,
         org.bukkit.ChatColor.DARK_BLUE,
         org.bukkit.ChatColor.DARK_GREEN,
         org.bukkit.ChatColor.DARK_AQUA,
         org.bukkit.ChatColor.DARK_RED,
         org.bukkit.ChatColor.DARK_PURPLE,
         org.bukkit.ChatColor.GOLD,
         org.bukkit.ChatColor.GRAY,
         org.bukkit.ChatColor.DARK_GRAY,
         org.bukkit.ChatColor.BLUE,
         org.bukkit.ChatColor.GREEN,
         org.bukkit.ChatColor.AQUA,
         org.bukkit.ChatColor.RED,
         org.bukkit.ChatColor.LIGHT_PURPLE,
         org.bukkit.ChatColor.YELLOW,
         org.bukkit.ChatColor.WHITE
      };

      for (int i = 0; i < legacyColors.length; i++) {
         double distance = Math.sqrt(
            Math.pow(color.getRed() - legacyColors[i][0], 2.0)
               + Math.pow(color.getGreen() - legacyColors[i][1], 2.0)
               + Math.pow(color.getBlue() - legacyColors[i][2], 2.0)
         );
         if (distance < minDistance) {
            minDistance = distance;
            nearest = colors[i];
         }
      }

      return nearest;
   }
}
