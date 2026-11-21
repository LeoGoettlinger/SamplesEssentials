package com.skyblockexp.ezrtp.util.compat;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;

public final class TextRenderer {
   private static boolean HAS_MINIMESSAGE;
   private static Object MINI_MESSAGE_INSTANCE;
   private static Method MINI_PARSE_METHOD;
   private static Method MINI_SERIALIZE_METHOD;
   private static Object LEGACY_SERIALIZER_INSTANCE;
   private static Method LEGACY_DESERIALIZE_METHOD;
   private static Method LEGACY_SERIALIZE_METHOD;
   private static boolean FORCE_LEGACY_COLORS = false;

   public static Component parse(String input) {
      if (input != null && !input.isBlank()) {
         if (HAS_MINIMESSAGE && MINI_PARSE_METHOD != null && MINI_MESSAGE_INSTANCE != null) {
            try {
               if (MINI_PARSE_METHOD.invoke(MINI_MESSAGE_INSTANCE, input) instanceof Component c) {
                  return c;
               }
            } catch (Throwable var4) {
            }
         }

         if (LEGACY_SERIALIZER_INSTANCE != null && LEGACY_DESERIALIZE_METHOD != null) {
            try {
               if (LEGACY_DESERIALIZE_METHOD.invoke(LEGACY_SERIALIZER_INSTANCE, input) instanceof Component c) {
                  return c;
               }
            } catch (Throwable var3) {
            }
         }

         return Component.text(input);
      } else {
         return Component.empty();
      }
   }

   public static String serialize(Component component) {
      if (component == null) {
         return "";
      } else {
         if (LEGACY_SERIALIZER_INSTANCE != null && LEGACY_SERIALIZE_METHOD != null) {
            try {
               if (LEGACY_SERIALIZE_METHOD.invoke(LEGACY_SERIALIZER_INSTANCE, component) instanceof String s) {
                  return s;
               }
            } catch (Throwable var6) {
            }
         }

         if (MINI_MESSAGE_INSTANCE != null && MINI_SERIALIZE_METHOD != null) {
            try {
               if (MINI_SERIALIZE_METHOD.invoke(MINI_MESSAGE_INSTANCE, component) instanceof String mmString) {
                  String legacy = miniMessageToLegacy(mmString);
                  if (legacy != null && !legacy.isBlank()) {
                     return legacy;
                  }

                  return mmString;
               }
            } catch (Throwable var5) {
            }
         }

         try {
            String s = component.toString();
            return s == null ? "" : s;
         } catch (Throwable var4) {
            return "";
         }
      }
   }

   private static String miniMessageToLegacy(String mm) {
      if (mm != null && !mm.isBlank()) {
         String out = mm;

         try {
            Pattern gradPat = Pattern.compile("(?i)<gradient:([^>]+)>(.*?)</gradient>", 32);
            Matcher gm = gradPat.matcher(out);
            StringBuffer gsb = new StringBuffer();

            while (gm.find()) {
               String colors = gm.group(1);
               String inner = gm.group(2);
               String firstHex = null;
               Matcher hexFinder = Pattern.compile("#([0-9a-fA-F]{6})").matcher(colors);
               if (hexFinder.find()) {
                  firstHex = hexFinder.group(1);
               }

               String replacement;
               if (firstHex != null) {
                  replacement = hexToNearestLegacy(firstHex) + inner + "§r";
               } else {
                  replacement = inner;
               }

               gm.appendReplacement(gsb, Matcher.quoteReplacement(replacement));
            }

            gm.appendTail(gsb);
            out = gsb.toString();
         } catch (Throwable var12) {
         }

         if (FORCE_LEGACY_COLORS) {
            try {
               Matcher m = Pattern.compile("<#([0-9a-fA-F]{6})>").matcher(out);
               StringBuffer sb = new StringBuffer();

               while (m.find()) {
                  String hex = m.group(1);
                  String code = hexToNearestLegacy(hex);
                  m.appendReplacement(sb, Matcher.quoteReplacement(code));
               }

               m.appendTail(sb);
               out = sb.toString();
               out = out.replaceAll("(?i)</#?[0-9a-fA-F]{0,6}>", "§r");
            } catch (Throwable var11) {
            }
         }

         try {
            out = out.replaceAll("(?i)<gradient>", "");
            out = out.replaceAll("(?i)</gradient>", "§r");
         } catch (Throwable var10) {
         }

         out = out.replaceAll("(?i)<black>", "§0");
         out = out.replaceAll("(?i)</black>", "§r");
         out = out.replaceAll("(?i)<dark_blue>", "§1");
         out = out.replaceAll("(?i)</dark_blue>", "§r");
         out = out.replaceAll("(?i)<dark_green>", "§2");
         out = out.replaceAll("(?i)</dark_green>", "§r");
         out = out.replaceAll("(?i)<dark_aqua>", "§3");
         out = out.replaceAll("(?i)</dark_aqua>", "§r");
         out = out.replaceAll("(?i)<dark_red>", "§4");
         out = out.replaceAll("(?i)</dark_red>", "§r");
         out = out.replaceAll("(?i)<dark_purple>", "§5");
         out = out.replaceAll("(?i)</dark_purple>", "§r");
         out = out.replaceAll("(?i)<gold>", "§6");
         out = out.replaceAll("(?i)</gold>", "§r");
         out = out.replaceAll("(?i)<gray>", "§7");
         out = out.replaceAll("(?i)</gray>", "§r");
         out = out.replaceAll("(?i)<dark_gray>", "§8");
         out = out.replaceAll("(?i)</dark_gray>", "§r");
         out = out.replaceAll("(?i)<blue>", "§9");
         out = out.replaceAll("(?i)</blue>", "§r");
         out = out.replaceAll("(?i)<green>", "§a");
         out = out.replaceAll("(?i)</green>", "§r");
         out = out.replaceAll("(?i)<aqua>", "§b");
         out = out.replaceAll("(?i)</aqua>", "§r");
         out = out.replaceAll("(?i)<red>", "§c");
         out = out.replaceAll("(?i)</red>", "§r");
         out = out.replaceAll("(?i)<light_purple>", "§d");
         out = out.replaceAll("(?i)</light_purple>", "§r");
         out = out.replaceAll("(?i)<yellow>", "§e");
         out = out.replaceAll("(?i)</yellow>", "§r");
         out = out.replaceAll("(?i)<white>", "§f");
         out = out.replaceAll("(?i)</white>", "§r");
         out = out.replaceAll("(?i)<bold>", "§l");
         out = out.replaceAll("(?i)</bold>", "§r");
         out = out.replaceAll("(?i)<italic>", "§o");
         out = out.replaceAll("(?i)</italic>", "§r");
         out = out.replaceAll("(?i)<underlined>", "§n");
         out = out.replaceAll("(?i)</underlined>", "§r");
         out = out.replaceAll("(?i)<strikethrough>", "§m");
         out = out.replaceAll("(?i)</strikethrough>", "§r");
         return out.replaceAll("(?i)<reset>", "§r");
      } else {
         return mm;
      }
   }

   private static String hexToNearestLegacy(String hex) {
      try {
         int r = Integer.parseInt(hex.substring(0, 2), 16);
         int g = Integer.parseInt(hex.substring(2, 4), 16);
         int b = Integer.parseInt(hex.substring(4, 6), 16);
         int[][] legacy = new int[][]{
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
         char[] codes = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
         double best = Double.MAX_VALUE;
         int idx = 15;

         for (int i = 0; i < legacy.length; i++) {
            int lr = legacy[i][0];
            int lg = legacy[i][1];
            int lb = legacy[i][2];
            double d = Math.pow(r - lr, 2.0) + Math.pow(g - lg, 2.0) + Math.pow(b - lb, 2.0);
            if (d < best) {
               best = d;
               idx = i;
            }
         }

         return "§" + codes[idx];
      } catch (Throwable var15) {
         return "";
      }
   }

   public static void setForceLegacyColors(boolean v) {
      FORCE_LEGACY_COLORS = v;
   }

   public static String miniMessageToLegacyString(String miniMessage) {
      if (miniMessage == null) {
         return "";
      } else {
         try {
            if (HAS_MINIMESSAGE
               && MINI_PARSE_METHOD != null
               && LEGACY_SERIALIZE_METHOD != null
               && MINI_MESSAGE_INSTANCE != null
               && LEGACY_SERIALIZER_INSTANCE != null) {
               Object comp = MINI_PARSE_METHOD.invoke(MINI_MESSAGE_INSTANCE, miniMessage);
               if (comp != null && LEGACY_SERIALIZE_METHOD.invoke(LEGACY_SERIALIZER_INSTANCE, comp) instanceof String s && s != null) {
                  return s;
               }
            }
         } catch (Throwable var4) {
         }

         String converted = miniMessageToLegacy(miniMessage);
         return converted == null ? "" : converted;
      }
   }

   static {
      Object mmInst = null;
      Method mmParse = null;
      boolean hasMm = false;
      Object legacyInst = null;
      Method legacyDeserialize = null;
      Method legacySerialize = null;

      try {
         Class<?> mmClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
         Method miniMessageFactory = mmClass.getMethod("miniMessage");
         mmInst = miniMessageFactory.invoke(null);

         try {
            mmParse = mmClass.getMethod("deserialize", String.class);
         } catch (Throwable var11) {
            mmParse = mmClass.getMethod("parse", String.class);
         }

         hasMm = mmInst != null && mmParse != null;
      } catch (Throwable var12) {
         hasMm = false;
      }

      try {
         Class<?> legacyClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
         Method legacyFactory = legacyClass.getMethod("legacySection");
         legacyInst = legacyFactory.invoke(null);
         legacyDeserialize = legacyInst.getClass().getMethod("deserialize", String.class);
         legacySerialize = legacyInst.getClass().getMethod("serialize", Component.class);
      } catch (Throwable var10) {
      }

      Method mmSerialize = null;
      if (hasMm && mmInst != null) {
         try {
            Class<?> mmClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            mmSerialize = mmClass.getMethod("serialize", Component.class);
         } catch (Throwable var9) {
            mmSerialize = null;
         }
      }

      HAS_MINIMESSAGE = hasMm;
      MINI_MESSAGE_INSTANCE = mmInst;
      MINI_PARSE_METHOD = mmParse;
      MINI_SERIALIZE_METHOD = mmSerialize;
      LEGACY_SERIALIZER_INSTANCE = legacyInst;
      LEGACY_DESERIALIZE_METHOD = legacyDeserialize;
      LEGACY_SERIALIZE_METHOD = legacySerialize;
   }
}
