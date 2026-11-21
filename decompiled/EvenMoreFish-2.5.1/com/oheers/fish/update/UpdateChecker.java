package com.oheers.fish.update;

import com.oheers.fish.EvenMoreFish;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.Contract;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jspecify.annotations.NonNull;

public class UpdateChecker {
   private final EvenMoreFish plugin;

   public UpdateChecker(final EvenMoreFish plugin) {
      this.plugin = plugin;
   }

   public String getVersion() {
      try {
         HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI("https://api.modrinth.com/v2/project/vlh7rLCf/version"))
            .header("User-Agent", "EvenMoreFish/" + this.plugin.getPluginMeta().getVersion())
            .timeout(Duration.ofSeconds(10L))
            .GET()
            .build();
         HttpResponse<String> response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
         if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode());
         } else {
            JSONArray versions = (JSONArray)new JSONParser().parse(response.body());
            if (versions.isEmpty()) {
               return this.plugin.getPluginMeta().getVersion();
            } else {
               JSONObject latestVersion = (JSONObject)versions.getFirst();
               String version = latestVersion.get("version_number").toString();
               int mcVersionSeparatorIndex = version.lastIndexOf("-");
               return mcVersionSeparatorIndex == -1 ? version : version.substring(0, mcVersionSeparatorIndex);
            }
         }
      } catch (Exception var7) {
         this.plugin.getLogger().warning("Update check failed: " + var7.getMessage());
         this.plugin.getLogger().info("Manual update check: https://modrinth.com/plugin/evenmorefish/versions");
         return this.plugin.getPluginMeta().getVersion();
      }
   }

   @Contract(" -> new")
   @NonNull
   public CompletableFuture<Boolean> checkUpdate() {
      return CompletableFuture.supplyAsync(() -> {
         ComparableVersion modrinthVersion = new ComparableVersion(new UpdateChecker(this.plugin).getVersion());
         ComparableVersion serverVersion = new ComparableVersion(this.plugin.getPluginMeta().getVersion());
         return modrinthVersion.compareTo(serverVersion) > 0;
      });
   }
}
