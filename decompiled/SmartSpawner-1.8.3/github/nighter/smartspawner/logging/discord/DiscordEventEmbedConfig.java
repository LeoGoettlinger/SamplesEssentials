package github.nighter.smartspawner.logging.discord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.configuration.ConfigurationSection;

public final class DiscordEventEmbedConfig {
   private final String title;
   private final String description;
   private final int color;
   private final String footer;
   private final List<DiscordWebhookConfig.EmbedField> fields;

   private DiscordEventEmbedConfig(String title, String description, int color, String footer, List<DiscordWebhookConfig.EmbedField> fields) {
      this.title = title;
      this.description = description;
      this.color = color;
      this.footer = footer;
      this.fields = fields;
   }

   public static DiscordEventEmbedConfig fromSection(ConfigurationSection cfg) {
      String title = cfg.getString("title", "{description}");
      String desc = cfg.getString("description", "{description}");
      String footer = cfg.getString("footer", "SmartSpawner • {time}");
      int color = parseColor(cfg.getString("color", "99AAB5"));
      List<DiscordWebhookConfig.EmbedField> fields = new ArrayList<>();

      for (Map<?, ?> fm : cfg.getMapList("fields")) {
         String name = (String)fm.get("name");
         String value = (String)fm.get("value");
         boolean inline = Boolean.TRUE.equals(fm.get("inline"));
         if (name != null && value != null) {
            fields.add(new DiscordWebhookConfig.EmbedField(name, value, inline));
         }
      }

      return new DiscordEventEmbedConfig(title, desc, color, footer, fields);
   }

   public static DiscordEventEmbedConfig defaults() {
      return new DiscordEventEmbedConfig("{event_type}", "{description}", 5793266, "SmartSpawner • {time}", List.of());
   }

   private static int parseColor(String hex) {
      if (hex == null) {
         return 5793266;
      } else {
         try {
            if (hex.startsWith("#")) {
               hex = hex.substring(1);
            }

            return Integer.parseInt(hex, 16);
         } catch (NumberFormatException var2) {
            return 5793266;
         }
      }
   }

   @Generated
   public String getTitle() {
      return this.title;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public int getColor() {
      return this.color;
   }

   @Generated
   public String getFooter() {
      return this.footer;
   }

   @Generated
   public List<DiscordWebhookConfig.EmbedField> getFields() {
      return this.fields;
   }
}
