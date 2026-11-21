package com.donutorders.util;

import com.donutorders.DonutOrders;
import com.donutorders.model.OrderStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public final class MessageHelper {
   private MessageHelper() {
   }

   public static String raw(String path, String def) {
      FileConfiguration messages = messages();
      if (messages == null) {
         return def != null ? def : "";
      } else {
         String value = messages.getString(path);
         if (value != null && !value.isEmpty()) {
            return value;
         } else {
            return def != null ? def : path;
         }
      }
   }

   public static String get(String path, String def) {
      return DonutOrders.colorize(raw(path, def));
   }

   public static String get(String path, String def, Object... args) {
      return DonutOrders.colorize(format(raw(path, def), args));
   }

   public static String getNamed(String path, String def, Map<String, String> placeholders) {
      return DonutOrders.colorize(formatNamed(raw(path, def), placeholders));
   }

   public static String getNamed(String path, String def, String... keyValues) {
      return DonutOrders.colorize(formatNamed(raw(path, def), toMap(keyValues)));
   }

   public static List<String> getList(String path) {
      return getList(path, Collections.emptyMap());
   }

   public static List<String> getList(String path, Map<String, String> placeholders) {
      FileConfiguration messages = messages();
      if (messages == null) {
         return Collections.emptyList();
      } else {
         List<String> raw = messages.getStringList(path);
         if (raw != null && !raw.isEmpty()) {
            List<String> out = new ArrayList<>(raw.size());

            for (String line : raw) {
               out.add(DonutOrders.colorize(formatNamed(line, placeholders)));
            }

            return out;
         } else {
            return Collections.emptyList();
         }
      }
   }

   public static List<String> getList(String path, String... keyValues) {
      return getList(path, toMap(keyValues));
   }

   public static List<String> getListPositional(String path, Object... args) {
      FileConfiguration messages = messages();
      if (messages == null) {
         return Collections.emptyList();
      } else {
         List<String> raw = messages.getStringList(path);
         if (raw != null && !raw.isEmpty()) {
            List<String> out = new ArrayList<>(raw.size());

            for (String line : raw) {
               out.add(DonutOrders.colorize(format(line, args)));
            }

            return out;
         } else {
            return Collections.emptyList();
         }
      }
   }

   public static String prefix() {
      return get("prefix", "&8[&b&lᴅᴏɴᴜᴛ&f&lᴏʀᴅᴇʀꜱ&8] &r");
   }

   public static void send(CommandSender sender, String path, String def, Object... args) {
      sender.sendMessage(get(path, def, args));
   }

   public static void sendPrefixed(CommandSender sender, String path, String def, Object... args) {
      sender.sendMessage(prefix() + get(path, def, args));
   }

   public static String format(String template, Object... args) {
      if (template == null) {
         return "";
      } else if (args != null && args.length != 0) {
         String result = template;

         for (int i = 0; i < args.length; i++) {
            String value = args[i] != null ? String.valueOf(args[i]) : "";
            result = result.replace("{" + i + "}", value);
         }

         return result;
      } else {
         return template;
      }
   }

   public static String formatNamed(String template, Map<String, String> placeholders) {
      if (template == null) {
         return "";
      } else if (placeholders != null && !placeholders.isEmpty()) {
         String result = template;

         for (Entry<String, String> e : placeholders.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", (CharSequence)(e.getValue() != null ? e.getValue() : ""));
         }

         return result;
      } else {
         return template;
      }
   }

   public static String formatExpiry(long expiresAtMillis) {
      long remaining = expiresAtMillis - System.currentTimeMillis();
      if (remaining <= 0L) {
         return raw("time.expired", "ᴇxᴘɪʀᴇᴅ");
      } else {
         long totalSeconds = remaining / 1000L;
         long days = totalSeconds / 86400L;
         long hours = totalSeconds % 86400L / 3600L;
         long minutes = totalSeconds % 3600L / 60L;
         String daySuffix = raw("time.days-suffix", "ᴅ");
         String hourSuffix = raw("time.hours-suffix", "ʜ");
         String minuteSuffix = raw("time.minutes-suffix", "ᴍ");
         StringBuilder sb = new StringBuilder();
         if (days > 0L) {
            sb.append(days).append(daySuffix).append(" ");
         }

         if (hours > 0L) {
            sb.append(hours).append(hourSuffix).append(" ");
         }

         if (minutes > 0L || sb.length() == 0) {
            sb.append(minutes).append(minuteSuffix);
         }

         return sb.toString().trim();
      }
   }

   public static String statusName(OrderStatus status) {
      if (status == null) {
         return "";
      } else {
         return switch (status) {
            case ACTIVE -> raw("status.active", "ᴀᴄᴛɪᴠᴇ");
            case COMPLETED -> raw("status.completed", "ᴄᴏᴍᴘʟᴇᴛᴇᴅ");
            case EXPIRED -> raw("status.expired", "ᴇxᴘɪʀᴇᴅ");
            case CANCELLED -> raw("status.cancelled", "ᴄᴀɴᴄᴇʟʟᴇᴅ");
            case PENDING -> raw("status.pending", "ᴘᴇɴᴅɪɴɢ ᴄᴏʟʟᴇᴄᴛɪᴏɴ");
            case CLAIMED -> raw("status.claimed", "ᴄʟᴀɪᴍᴇᴅ");
         };
      }
   }

   public static String statusColor(OrderStatus status) {
      if (status == null) {
         return "§7";
      } else {
         String key = switch (status) {
            case ACTIVE -> "status.color.active";
            case COMPLETED -> "status.color.completed";
            case EXPIRED -> "status.color.expired";
            case CANCELLED -> "status.color.cancelled";
            case PENDING -> "status.color.pending";
            case CLAIMED -> "status.color.claimed";
         };

         String def = switch (status) {
            case ACTIVE -> "&a";
            case COMPLETED -> "&b";
            case EXPIRED -> "&6";
            case CANCELLED -> "&c";
            case PENDING -> "&e";
            case CLAIMED -> "&8";
         };
         return DonutOrders.colorize(raw(key, def));
      }
   }

   public static String cancelKeyword() {
      return raw("chat-cancel-keyword", "cancel");
   }

   private static FileConfiguration messages() {
      DonutOrders plugin = DonutOrders.getInstance();
      return plugin != null ? plugin.getMessages() : null;
   }

   private static Map<String, String> toMap(String... keyValues) {
      if (keyValues != null && keyValues.length != 0) {
         LinkedHashMap<String, String> map = new LinkedHashMap<>();

         for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
         }

         return map;
      } else {
         return Collections.emptyMap();
      }
   }
}
