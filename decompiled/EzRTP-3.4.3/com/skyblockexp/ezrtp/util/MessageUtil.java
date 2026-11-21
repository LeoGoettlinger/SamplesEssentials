package com.skyblockexp.ezrtp.util;

import com.skyblockexp.ezrtp.message.MessageFormatter;
import com.skyblockexp.ezrtp.platform.PlatformMessageService;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class MessageUtil {
   private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
   private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().hexColors().character('&').build();
   private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.builder().hexColors().character('§').build();
   private static final Pattern MINIMESSAGE_TAG_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*(:.*)?");
   private static String prefix = "";
   private static final Logger LOGGER = Logger.getLogger("EzRTP");
   private static boolean FORCE_LEGACY_COLORS = false;

   private MessageUtil() {
   }

   public static void setForceLegacyColors(boolean v) {
      FORCE_LEGACY_COLORS = v;
   }

   public static String miniMessageToLegacyString(String miniMessage) {
      if (miniMessage == null) {
         return "";
      } else {
         try {
            Component comp = MINI_MESSAGE.deserialize(miniMessage);
            String out = SECTION_SERIALIZER.serialize(comp);
            if (out != null) {
               return out;
            }
         } catch (Throwable var3) {
         }

         return miniMessageToLegacy(miniMessage);
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

   public static Component parseLegacySection(String legacy) {
      if (legacy != null && !legacy.isBlank()) {
         try {
            return SECTION_SERIALIZER.deserialize(legacy);
         } catch (Throwable var4) {
            try {
               return Component.text(legacy);
            } catch (Throwable var3) {
               return Component.empty();
            }
         }
      } else {
         return Component.empty();
      }
   }

   public static String translateLegacyColors(String text) {
      return text != null && !text.isEmpty() ? text.replace('&', '§') : "";
   }

   public static String translateColors(String text) {
      if (text == null || text.isEmpty()) {
         return "";
      } else if (containsMiniMessageTags(text)) {
         try {
            Component component = MINI_MESSAGE.deserialize(text);
            return SECTION_SERIALIZER.serialize(component);
         } catch (Throwable var2) {
            return translateLegacyColors(text);
         }
      } else {
         return translateLegacyColors(text);
      }
   }

   private static boolean containsMiniMessageTags(String text) {
      if (text != null && !text.isEmpty()) {
         int length = text.length();

         for (int i = 0; i < length - 1; i++) {
            if (text.charAt(i) == '<') {
               int closeIdx = text.indexOf(62, i + 1);
               if (closeIdx > i + 1 && closeIdx < i + 30) {
                  String potential = text.substring(i + 1, closeIdx);
                  if (MINIMESSAGE_TAG_PATTERN.matcher(potential).matches()) {
                     return true;
                  }

                  if (potential.startsWith("#") && potential.length() <= 7) {
                     String hex = potential.substring(1);
                     if (hex.matches("(?i:[0-9a-f]{1,6})")) {
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static String stripColors(String text) {
      if (text != null && !text.isEmpty()) {
         try {
            return text.replaceAll("(?i)§[0-9A-FK-OR]", "");
         } catch (Throwable var2) {
            return text;
         }
      } else {
         return "";
      }
   }

   public static Component parseMiniMessage(String miniMessage) {
      return (Component)(miniMessage != null && !miniMessage.isEmpty() ? MINI_MESSAGE.deserialize(miniMessage) : Component.empty());
   }

   public static String serializeToMiniMessage(Component component) {
      return component == null ? "" : MINI_MESSAGE.serialize(component);
   }

   public static Component legacyToComponent(String legacyText) {
      return legacyText != null && !legacyText.isEmpty() ? LEGACY_SERIALIZER.deserialize(legacyText) : Component.empty();
   }

   public static String componentToLegacy(Component component) {
      return component == null ? "" : SECTION_SERIALIZER.serialize(component);
   }

   public static String ampersandToSection(String text) {
      return translateLegacyColors(text);
   }

   public static void setPrefix(String newPrefix) {
      if (newPrefix == null) {
         prefix = "";
      } else {
         prefix = newPrefix.trim();
      }
   }

   public static String getPrefix() {
      return prefix;
   }

   public static void send(Object recipient, String message) {
      if (recipient != null && message != null) {
         Component prefixComp = (Component)(prefix.isEmpty() ? Component.empty() : MessageFormatter.format(prefix));

         String resolved;
         try {
            PlatformMessageService svc = PlatformMessageServiceRegistry.get();
            if (svc != null) {
               resolved = svc.resolvePlaceholders(recipient, message, LOGGER);
            } else {
               resolved = PlaceholderUtil.resolvePlaceholders(recipient, message, LOGGER);
            }
         } catch (Throwable var10) {
            resolved = PlaceholderUtil.resolvePlaceholders(recipient, message, LOGGER);
         }

         Component messageComp = MessageFormatter.format(resolved);
         Component full = prefixComp.append(messageComp);

         try {
            PlatformMessageService svc = PlatformMessageServiceRegistry.get();
            if (svc != null && svc.sendToSender(recipient, full)) {
               return;
            }
         } catch (Throwable var9) {
         }

         String out = serializeComponent(full);
         out = extractLegacySubstringIfNeeded(out);

         try {
            recipient.getClass().getMethod("sendMessage", String.class).invoke(recipient, out);
         } catch (Throwable var8) {
         }
      }
   }

   public static void send(Object recipient, Component message) {
      if (recipient != null && message != null) {
         Component prefixComp = (Component)(prefix.isEmpty() ? Component.empty() : MessageFormatter.format(prefix));
         Component resolvedMessage = message;

         try {
            PlatformMessageService svc = PlatformMessageServiceRegistry.get();
            if (svc != null) {
               resolvedMessage = svc.resolvePlaceholdersComponent(recipient, message, LOGGER);
            } else {
               String mm = serializeToMiniMessage(message);
               String replaced = PlaceholderUtil.resolvePlaceholders(recipient, mm, LOGGER);
               resolvedMessage = parseMiniMessage(replaced);
            }
         } catch (Throwable var9) {
         }

         Component full = prefixComp.append(resolvedMessage);

         try {
            PlatformMessageService svc = PlatformMessageServiceRegistry.get();
            if (svc != null && svc.sendToSender(recipient, full)) {
               return;
            }
         } catch (Throwable var8) {
         }

         String out = serializeComponent(full);
         out = extractLegacySubstringIfNeeded(out);

         try {
            recipient.getClass().getMethod("sendMessage", String.class).invoke(recipient, out);
         } catch (Throwable var7) {
         }
      }
   }

   public static void broadcast(String message) {
      try {
         Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
         Method getOnline = bukkit.getMethod("getOnlinePlayers");
         Object players = getOnline.invoke(null);
         if (players instanceof Iterable) {
            Iterator var4 = ((Iterable)players).iterator();

            while (true) {
               Object p;
               Component comp;
               while (true) {
                  if (!var4.hasNext()) {
                     return;
                  }

                  p = var4.next();

                  String resolved;
                  try {
                     PlatformMessageService svc = PlatformMessageServiceRegistry.get();
                     if (svc != null) {
                        resolved = svc.resolvePlaceholders(p, prefix + message, LOGGER);
                     } else {
                        resolved = PlaceholderUtil.resolvePlaceholders(p, prefix + message, LOGGER);
                     }
                  } catch (Throwable var11) {
                     resolved = PlaceholderUtil.resolvePlaceholders(p, prefix + message, LOGGER);
                  }

                  comp = MessageFormatter.format(resolved);

                  try {
                     PlatformMessageService svc = PlatformMessageServiceRegistry.get();
                     if (svc == null || !svc.sendToSender(p, comp)) {
                        break;
                     }
                  } catch (Throwable var12) {
                     break;
                  }
               }

               String out = serializeComponent(comp);
               out = extractLegacySubstringIfNeeded(out);

               try {
                  p.getClass().getMethod("sendMessage", String.class).invoke(p, out);
               } catch (Throwable var10) {
               }
            }
         }
      } catch (Throwable var13) {
      }
   }

   public static void broadcast(Component message) {
      Component prefixComp = (Component)(prefix.isEmpty() ? Component.empty() : MessageFormatter.format(prefix));
      Component fullComp = prefixComp.append(message);

      try {
         Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
         Method getOnline = bukkit.getMethod("getOnlinePlayers");
         Object players = getOnline.invoke(null);
         if (players instanceof Iterable) {
            for (Object p : (Iterable)players) {
               Component resolved = fullComp;

               try {
                  PlatformMessageService svc = PlatformMessageServiceRegistry.get();
                  if (svc != null) {
                     resolved = svc.resolvePlaceholdersComponent(p, fullComp, LOGGER);
                  } else {
                     String mm = serializeToMiniMessage(fullComp);
                     String replaced = PlaceholderUtil.resolvePlaceholders(p, mm, LOGGER);
                     resolved = parseMiniMessage(replaced);
                  }
               } catch (Throwable var14) {
               }

               try {
                  PlatformMessageService svc = PlatformMessageServiceRegistry.get();
                  if (svc == null || !svc.sendToSender(p, resolved)) {
                     try {
                        p.getClass().getMethod("sendMessage", String.class).invoke(p, serializeComponent(resolved));
                     } catch (Throwable var13) {
                     }
                  }
               } catch (Throwable var15) {
                  try {
                     p.getClass().getMethod("sendMessage", String.class).invoke(p, serializeComponent(resolved));
                  } catch (Throwable var12) {
                  }
               }
            }
         }
      } catch (Throwable var16) {
      }
   }

   public static void log(Logger logger, String message, Level level) {
      if (logger != null && message != null) {
         logger.log(level != null ? level : Level.INFO, stripColors(prefix + message));
      }
   }

   public static void log(Logger logger, Component message, Level level) {
      if (logger != null && message != null) {
         logger.log(level != null ? level : Level.INFO, stripColors(prefix + serializeComponent(message)));
      }
   }

   public static String serialize(String message) {
      if (message == null) {
         return "";
      } else {
         Component comp = MessageFormatter.format(message);
         return serializeComponent(comp);
      }
   }

   public static String serialize(String prefix, Component message) {
      if (message == null) {
         return "";
      } else {
         Component prefixComp = (Component)(prefix != null && !prefix.isEmpty() ? MessageFormatter.format(prefix) : Component.empty());
         Component full = prefixComp.append(message);
         return serializeComponent(full);
      }
   }

   public static String serializeComponent(Component message) {
      if (message == null) {
         return "";
      } else {
         try {
            return SECTION_SERIALIZER.serialize(message);
         } catch (Throwable var6) {
            try {
               return MINI_MESSAGE.serialize(message);
            } catch (Throwable var5) {
               try {
                  return message.toString();
               } catch (Throwable var4) {
                  return "";
               }
            }
         }
      }
   }

   public static String stripColorsForLog(String input) {
      return MessageFormatter.stripColors(input);
   }

   private static String extractLegacySubstringIfNeeded(String out) {
      if (out != null && out.contains("TextComponentImpl")) {
         int first = out.indexOf(167);
         if (first >= 0) {
            int last = out.lastIndexOf("§r");
            if (last >= 0 && last + 2 > first) {
               out = out.substring(first, last + 2);
            } else {
               out = out.substring(first);
            }

            if (out.endsWith("§r")) {
               out = out.substring(0, out.length() - 2);
            }
         }
      }

      return out;
   }

   public static List<Component> toComponentList(List<Component> list) {
      return list;
   }
}
