package eu.kennytv.maintenance.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.kennytv.maintenance.core.Settings;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public final class DiscordWebhook {
   private static final int COLOR_MAINTENANCE_ENABLED = 15548997;
   private static final int COLOR_MAINTENANCE_DISABLED = 5763719;
   private static final int COLOR_STARTTIMER = 16705372;
   private static final int COLOR_ENDTIMER = 5793266;
   private static final int COLOR_BLOCKED_JOIN = 15105570;

   public static void sendMessage(String message, DiscordWebhook.EventType event, Settings settings) throws IOException, InterruptedException {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("username", settings.getWebhookUsername());
      String avatarUrl = settings.getWebhookAvatarUrl();
      if (!avatarUrl.isEmpty()) {
         jsonObject.addProperty("avatar_url", avatarUrl);
      }

      JsonObject embed = new JsonObject();
      embed.addProperty("description", message);
      embed.addProperty("color", getColorForEvent(event));
      JsonArray embeds = new JsonArray();
      embeds.add(embed);
      jsonObject.add("embeds", embeds);

      try (HttpClient client = HttpClient.newHttpClient()) {
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(settings.getWebhookUrl()))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(jsonObject.toString()))
            .build();
         client.send(request, BodyHandlers.discarding());
      }
   }

   private static int getColorForEvent(DiscordWebhook.EventType event) {
      return switch (event) {
         case MAINTENANCE_ENABLED -> 15548997;
         case MAINTENANCE_DISABLED -> 5763719;
         case STARTTIMER_STARTED -> 16705372;
         case ENDTIMER_STARTED -> 5793266;
         case BLOCKED_JOIN -> 15105570;
      };
   }

   public static enum EventType {
      MAINTENANCE_ENABLED("maintenance_status"),
      MAINTENANCE_DISABLED("maintenance_status"),
      STARTTIMER_STARTED("starttimer_started"),
      ENDTIMER_STARTED("endtimer_started"),
      BLOCKED_JOIN("blocked_join");

      private final String configKey;

      private EventType(final String configKey) {
         this.configKey = configKey;
      }

      public String configKey() {
         return this.configKey;
      }
   }
}
