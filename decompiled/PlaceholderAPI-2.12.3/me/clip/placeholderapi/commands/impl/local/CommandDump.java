package me.clip.placeholderapi.commands.impl.local;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandDump extends PlaceholderCommand {
   @NotNull
   private static final String URL = "https://paste.helpch.at/";
   @NotNull
   private static final Gson gson = new Gson();
   @NotNull
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
      .withLocale(Locale.US)
      .withZone(ZoneId.of("UTC"));

   public CommandDump() {
      super("dump");
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      this.postDump(this.makeDump(plugin)).whenComplete((key, exception) -> {
         if (exception != null) {
            plugin.getLogger().log(Level.WARNING, "failed to post dump details", exception);
            Msg.msg(sender, "&cFailed to post dump details, check console.");
         } else {
            Msg.msg(sender, "&aSuccessfully posted dump: https://paste.helpch.at/" + key);
         }
      });
   }

   @NotNull
   private CompletableFuture<String> postDump(@NotNull String dump) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            HttpURLConnection connection = (HttpURLConnection)new URL("https://paste.helpch.at/documents").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            connection.setDoOutput(true);
            connection.connect();
            OutputStream stream = connection.getOutputStream();

            try {
               stream.write(dump.getBytes(StandardCharsets.UTF_8));
            } catch (Throwable var8) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var6) {
                     var8.addSuppressed(var6);
                  }
               }

               throw var8;
            }

            if (stream != null) {
               stream.close();
            }

            InputStream streamx = connection.getInputStream();

            String var4;
            try {
               String json = CharStreams.toString(new InputStreamReader(streamx, StandardCharsets.UTF_8));
               var4 = ((JsonObject)gson.fromJson(json, JsonObject.class)).get("key").getAsString();
            } catch (Throwable var7) {
               if (streamx != null) {
                  try {
                     streamx.close();
                  } catch (Throwable var5) {
                     var7.addSuppressed(var5);
                  }
               }

               throw var7;
            }

            if (streamx != null) {
               streamx.close();
            }

            return var4;
         } catch (IOException var9) {
            throw new CompletionException(var9);
         }
      });
   }

   @NotNull
   private String makeDump(@NotNull PlaceholderAPIPlugin plugin) {
      StringBuilder builder = new StringBuilder();
      builder.append("Generated: ").append(DATE_FORMAT.format(Instant.now())).append("\n\n");
      builder.append("PlaceholderAPI: ").append(plugin.getDescription().getVersion()).append("\n\n");
      builder.append("Expansions Registered:").append('\n');
      List<PlaceholderExpansion> expansions = plugin.getLocalExpansionManager()
         .getExpansions()
         .stream()
         .sorted(Comparator.comparing(PlaceholderExpansion::getIdentifier).thenComparing(PlaceholderExpansion::getAuthor))
         .collect(Collectors.toList());
      int size = expansions.stream().map(e -> e.getIdentifier().length()).max(Integer::compareTo).orElse(0);

      for (PlaceholderExpansion expansion : expansions) {
         builder.append("  ")
            .append(String.format("%-" + size + "s", expansion.getIdentifier()))
            .append(" [Author: ")
            .append(expansion.getAuthor())
            .append(", Version: ")
            .append(expansion.getVersion())
            .append("]\n");
      }

      builder.append('\n');
      builder.append("Expansions Directory:").append('\n');
      String[] jars = plugin.getLocalExpansionManager().getExpansionsFolder().list((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".jar"));
      if (jars == null) {
         builder.append("  ¨[Warning]: Could not load jar files from expansions folder.");
      } else {
         for (String jar : jars) {
            builder.append("  ").append(jar).append('\n');
         }
      }

      builder.append('\n');
      builder.append("Server Info: ").append(plugin.getServer().getBukkitVersion()).append('/').append(plugin.getServer().getVersion()).append("\n");
      builder.append("Java Version: ").append(System.getProperty("java.version")).append("\n\n");
      builder.append("Plugin Info:").append('\n');
      List<Plugin> plugins = Arrays.stream(plugin.getServer().getPluginManager().getPlugins())
         .sorted(Comparator.comparing(Plugin::getName))
         .collect(Collectors.toList());
      size = plugins.stream().map(pl -> pl.getName().length()).max(Integer::compareTo).orElse(0);

      for (Plugin other : plugins) {
         builder.append("  ")
            .append(String.format("%-" + size + "s", other.getName()))
            .append(" [Authors: [")
            .append(String.join(", ", other.getDescription().getAuthors()))
            .append("], Version: ")
            .append(other.getDescription().getVersion())
            .append("]")
            .append("\n");
      }

      return builder.toString();
   }
}
