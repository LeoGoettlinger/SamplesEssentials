package github.nighter.smartspawner.logging.discord;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.logging.SpawnerLogEntry;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;

public class DiscordEmbedBuilder {
   private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
   private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_INSTANT;

   public static DiscordEmbed buildEmbed(SpawnerLogEntry entry, DiscordEventEmbedConfig embedCfg, DiscordWebhookConfig globalCfg, SmartSpawner plugin) {
      Map<String, String> placeholders = buildPlaceholders(entry, embedCfg);
      return buildYamlEmbed(entry, embedCfg, globalCfg, placeholders);
   }

   public static String buildWebhookPayload(SpawnerLogEntry entry, DiscordEventEmbedConfig embedCfg, DiscordWebhookConfig globalCfg, SmartSpawner plugin) {
      return buildEmbed(entry, embedCfg, globalCfg, plugin).toJson();
   }

   private static DiscordEmbed buildYamlEmbed(
      SpawnerLogEntry entry, DiscordEventEmbedConfig embedCfg, DiscordWebhookConfig globalCfg, Map<String, String> placeholders
   ) {
      DiscordEmbed embed = new DiscordEmbed();
      embed.setColor(embedCfg.getColor());
      embed.setTitle(replacePlaceholders(embedCfg.getTitle(), placeholders));
      embed.setDescription(buildCompactDescription(entry, placeholders, embedCfg));
      embed.setFooter(
         replacePlaceholders(embedCfg.getFooter(), placeholders), "https://images.minecraft-heads.com/render2d/head/2e/2eaa2d8b7e9a098ebd33fcb6cf1120f4.webp"
      );
      embed.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
      if (globalCfg.isShowPlayerHead() && entry.getPlayerName() != null) {
         embed.setThumbnail(getPlayerAvatarUrl(entry.getPlayerName()));
      }

      for (DiscordWebhookConfig.EmbedField f : embedCfg.getFields()) {
         embed.addField(replacePlaceholders(f.getName(), placeholders), replacePlaceholders(f.getValue(), placeholders), f.isInline());
      }

      if (embedCfg.getFields().isEmpty()) {
         addCompactFields(embed, entry);
      }

      return embed;
   }

   private static String buildCompactDescription(SpawnerLogEntry entry, Map<String, String> placeholders, DiscordEventEmbedConfig embedCfg) {
      StringBuilder desc = new StringBuilder();
      desc.append(replacePlaceholders(embedCfg.getDescription(), placeholders));
      desc.append("\n\n");
      if (entry.getPlayerName() != null) {
         desc.append("\ud83d\udc64 `").append(entry.getPlayerName()).append("`");
      }

      if (entry.getLocation() != null) {
         Location loc = entry.getLocation();
         if (entry.getPlayerName() != null) {
            desc.append(" • ");
         }

         desc.append("\ud83d\udccd `")
            .append(loc.getWorld().getName())
            .append(" (")
            .append(loc.getBlockX())
            .append(", ")
            .append(loc.getBlockY())
            .append(", ")
            .append(loc.getBlockZ())
            .append(")`");
      }

      if (entry.getEntityType() != null) {
         desc.append("\n\ud83d\udc3e `").append(formatEntityName(entry.getEntityType().name())).append("`");
      }

      return desc.toString();
   }

   private static void addCompactFields(DiscordEmbed embed, SpawnerLogEntry entry) {
      int count = 0;

      for (Entry<String, Object> meta : entry.getMetadata().entrySet()) {
         if (count++ >= 6) {
            break;
         }

         String icon = getFieldIcon(meta.getKey());
         embed.addField(icon + " " + formatFieldName(meta.getKey()), formatCompactValue(meta.getValue()), true);
      }
   }

   private static Map<String, String> buildPlaceholders(SpawnerLogEntry entry, DiscordEventEmbedConfig embedCfg) {
      Map<String, String> p = new HashMap<>();
      Instant now = Instant.ofEpochMilli(System.currentTimeMillis());
      p.put("description", entry.getEventType().getDescription());
      p.put("event_type", entry.getEventType().name());
      p.put("time", TIME_FMT.format(now));
      p.put("timestamp", ISO_FMT.format(now));
      p.put("color", String.valueOf(embedCfg.getColor()));
      p.put("player", entry.getPlayerName() != null ? entry.getPlayerName() : "N/A");
      if (entry.getPlayerUuid() != null) {
         p.put("player_uuid", entry.getPlayerUuid().toString());
      }

      if (entry.getLocation() != null) {
         Location loc = entry.getLocation();
         p.put("world", loc.getWorld().getName());
         p.put("x", String.valueOf(loc.getBlockX()));
         p.put("y", String.valueOf(loc.getBlockY()));
         p.put("z", String.valueOf(loc.getBlockZ()));
         p.put("location", String.format("%s (%d, %d, %d)", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
      }

      for (Entry<String, Object> meta : entry.getMetadata().entrySet()) {
         p.put(meta.getKey(), String.valueOf(meta.getValue()));
      }

      p.put("entity", resolveEntityPlaceholder(entry, p));
      return p;
   }

   private static String replacePlaceholders(String text, Map<String, String> ph) {
      if (text == null) {
         return "";
      } else {
         for (Entry<String, String> e : ph.entrySet()) {
            text = text.replace("{" + e.getKey() + "}", e.getValue());
         }

         return text;
      }
   }

   private static String getPlayerAvatarUrl(String playerName) {
      return "https://mc-heads.net/avatar/" + playerName + "/64.png";
   }

   private static String formatCompactValue(Object value) {
      if (value == null) {
         return "`N/A`";
      } else if (!(value instanceof Double) && !(value instanceof Float)) {
         String s = String.valueOf(value);
         return "`" + (s.length() > 50 ? s.substring(0, 47) + "..." : s) + "`";
      } else {
         return "`" + String.format("%.2f", ((Number)value).doubleValue()) + "`";
      }
   }

   private static String formatFieldName(String raw) {
      StringBuilder sb = new StringBuilder();

      for (String word : raw.split("_")) {
         if (!word.isEmpty()) {
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
               sb.append(word.substring(1).toLowerCase());
            }

            sb.append(' ');
         }
      }

      return sb.toString().trim();
   }

   private static String formatEntityName(String name) {
      if (name != null && !name.isEmpty()) {
         StringBuilder sb = new StringBuilder();

         for (String w : name.toLowerCase().split("_")) {
            if (!w.isEmpty()) {
               sb.append(Character.toUpperCase(w.charAt(0)));
               if (w.length() > 1) {
                  sb.append(w.substring(1));
               }

               sb.append(' ');
            }
         }

         return sb.toString().trim();
      } else {
         return name;
      }
   }

   private static String resolveEntityPlaceholder(SpawnerLogEntry entry, Map<String, String> placeholders) {
      String itemType = placeholders.get("item_type");
      if (itemType != null && !itemType.isBlank()) {
         return formatEntityName(itemType);
      } else {
         return entry.getEntityType() != null ? formatEntityName(entry.getEntityType().name()) : "N/A";
      }
   }

   private static String getFieldIcon(String key) {
      String l = key.toLowerCase();
      if (l.contains("command")) {
         return "⚙️";
      } else if (l.contains("amount") || l.contains("count")) {
         return "\ud83d\udd22";
      } else if (l.contains("price") || l.contains("cost") || l.contains("money")) {
         return "\ud83d\udcb0";
      } else if (l.contains("exp")) {
         return "✨";
      } else if (l.contains("stack")) {
         return "\ud83d\udcda";
      } else {
         return l.contains("type") ? "\ud83c\udff7️" : "•";
      }
   }
}
