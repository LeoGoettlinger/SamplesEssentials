package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandContext;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.TextComponent;
import ac.grim.grimac.shaded.kyori.adventure.text.event.ClickEvent;
import ac.grim.grimac.shaded.kyori.adventure.text.format.NamedTextColor;
import ac.grim.grimac.shaded.kyori.adventure.text.format.TextDecoration;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import ac.grim.grimac.utils.common.arguments.CommonGrimArguments;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Generated;

public class GrimVersion implements BuildableCommand {
   private static final AtomicReference<Component> updateMessage = new AtomicReference<>();
   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.of(CommonGrimArguments.URL_TIMEOUT.value().intValue(), ChronoUnit.MILLIS))
      .build();
   private static long lastCheck;

   public static void checkForUpdatesAsync(Sender sender) {
      String current = GrimAPI.INSTANCE.getExternalAPI().getGrimVersion();
      sender.sendMessage(
         Component.text()
            .append(Component.text("Grim Version: ").color(NamedTextColor.GRAY))
            .append(Component.text(current).color(NamedTextColor.AQUA))
            .build()
      );
      long now = System.currentTimeMillis();
      if (now - lastCheck < 60000L) {
         Component message = updateMessage.get();
         if (message != null) {
            sender.sendMessage(message);
         }
      } else {
         lastCheck = now;
         GrimAPI.INSTANCE.getScheduler().getAsyncScheduler().runNow(GrimAPI.INSTANCE.getGrimPlugin(), () -> checkForUpdates(sender));
      }
   }

   private static void checkForUpdates(Sender sender) {
      try {
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(CommonGrimArguments.API_URL.value() + "updates"))
            .GET()
            .header("User-Agent", "GrimAC/" + GrimAPI.INSTANCE.getExternalAPI().getGrimVersion())
            .header("Content-Type", "application/json")
            .timeout(Duration.of(CommonGrimArguments.URL_TIMEOUT.value().intValue(), ChronoUnit.MILLIS))
            .build();
         HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
         int statusCode = response.statusCode();
         if (statusCode < 200 || statusCode >= 300) {
            Component msg = updateMessage.get();
            sender.sendMessage(
               Objects.requireNonNullElseGet(
                  msg,
                  () -> Component.text()
                     .append(MessageUtil.miniMessage("%prefix%"))
                     .append(Component.text(" Failed to check latest GrimAC version. Update server responded with status code: ").color(NamedTextColor.YELLOW))
                     .append(((TextComponent)Component.text(statusCode).color(getColorForStatusCode(statusCode))).decorate(TextDecoration.BOLD))
                     .build()
               )
            );
            return;
         }

         JsonObject object = new JsonParser().parse(response.body()).getAsJsonObject();
         String downloadPage = getJsonString(object, "download_page", "Unknown");
         String latest = getJsonString(object, "latest_version", "Unknown");
         String warning = getJsonString(object, "warning", null);
         GrimVersion.Status status;
         if (object.has("status")) {
            status = GrimVersion.Status.getStatus(object.get("status").getAsString());
         } else {
            status = GrimVersion.Status.SemVer.getVersionStatus(GrimAPI.INSTANCE.getExternalAPI().getGrimVersion(), latest);
         }
         Component msg = switch (status) {
            case AHEAD -> (TextComponent)Component.text("You are using a development version of GrimAC").color(NamedTextColor.LIGHT_PURPLE);
            case UPDATED -> (TextComponent)Component.text("You are using the latest version of GrimAC").color(NamedTextColor.GREEN);
            case OUTDATED -> (TextComponent)Component.text()
               .append(Component.text("New GrimAC version found!").color(NamedTextColor.AQUA))
               .append(Component.text(" Version ").color(NamedTextColor.GRAY))
               .append(((TextComponent)Component.text(latest).color(NamedTextColor.GRAY)).decorate(TextDecoration.ITALIC))
               .append(Component.text(" is available to be downloaded here: ").color(NamedTextColor.GRAY))
               .append(
                  ((TextComponent)((TextComponent)Component.text(downloadPage).color(NamedTextColor.GRAY)).decorate(TextDecoration.UNDERLINED))
                     .clickEvent(ClickEvent.openUrl(downloadPage))
               )
               .build();
            case UNKNOWN -> (TextComponent)Component.text("You are using an unknown GrimAC version.").color(NamedTextColor.RED);
         };
         if (warning != null && !warning.isBlank()) {
            msg = msg.append((Component)Component.text().append(Component.text(warning).color(NamedTextColor.RED)).build());
         }

         updateMessage.set(msg);
         sender.sendMessage(msg);
      } catch (Exception var10) {
         sender.sendMessage(Component.text("Failed to check latest version.").color(NamedTextColor.RED));
         LogUtil.error("Failed to check latest GrimAC version.", var10);
      }
   }

   private static String getJsonString(JsonObject object, String key, String defaultValue) {
      return object.has(key) ? object.get(key).getAsString() : defaultValue;
   }

   private static NamedTextColor getColorForStatusCode(int code) {
      if (code >= 500) {
         return NamedTextColor.RED;
      } else if (code >= 400) {
         return NamedTextColor.RED;
      } else if (code >= 300) {
         return NamedTextColor.YELLOW;
      } else {
         return code >= 200 ? NamedTextColor.GREEN : NamedTextColor.GRAY;
      }
   }

   @Override
   public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
      commandManager.command(
         commandManager.commandBuilder("grim", new String[]{"grimac"}).literal("version").permission("grim.version").handler(this::handleVersion)
      );
   }

   private void handleVersion(@NotNull CommandContext<Sender> context) {
      Sender sender = context.sender();
      checkForUpdatesAsync(sender);
   }

   private static enum Status {
      AHEAD("ahead"),
      UPDATED("updated"),
      OUTDATED("outdated"),
      UNKNOWN("unknown");

      private final String id;

      public static GrimVersion.Status getStatus(String id) {
         for (GrimVersion.Status status : values()) {
            if (status.id.equals(id)) {
               return status;
            }
         }

         return UNKNOWN;
      }

      @Generated
      private Status(final String id) {
         this.id = id;
      }

      private static class SemVer {
         public static GrimVersion.Status getVersionStatus(String current, String latest) {
            try {
               int cmp = compareSemver(current, latest);
               if (cmp == 0) {
                  return GrimVersion.Status.UPDATED;
               } else {
                  return cmp < 0 ? GrimVersion.Status.OUTDATED : GrimVersion.Status.AHEAD;
               }
            } catch (Exception var3) {
               return GrimVersion.Status.UNKNOWN;
            }
         }

         public static String normalizeCoreVersion(String version) {
            String trimmed = version.trim();
            String[] dashParts = trimmed.split("-");
            String[] plusParts = dashParts[0].split("\\+");
            return plusParts[0];
         }

         public static int[] parseVersion(String version) {
            String core = normalizeCoreVersion(version);
            if (core.isEmpty()) {
               return null;
            } else {
               String[] parts = core.split("\\.");
               if (parts.length < 1) {
                  return null;
               } else {
                  int major = parseInt(parts[0]);
                  int minor = parts.length > 1 ? parseInt(parts[1]) : 0;
                  int patch = parts.length > 2 ? parseInt(parts[2]) : 0;
                  return major >= 0 && minor >= 0 && patch >= 0 ? new int[]{major, minor, patch} : null;
               }
            }
         }

         private static int parseInt(String str) {
            try {
               return Integer.parseInt(str);
            } catch (NumberFormatException var2) {
               return -1;
            }
         }

         public static int compareSemver(String a, String b) {
            int[] pa = parseVersion(a);
            int[] pb = parseVersion(b);
            if (pa != null && pb != null) {
               for (int i = 0; i < 3; i++) {
                  if (pa[i] < pb[i]) {
                     return -1;
                  }

                  if (pa[i] > pb[i]) {
                     return 1;
                  }
               }

               return 0;
            } else {
               return 0;
            }
         }
      }
   }
}
