package com.artillexstudios.axtrade.lang;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.libs.axapi.config.Config;
import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Base64;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class LanguageManager {
   private static Config translations;
   private static final Gson gson = new GsonBuilder().create();

   public static void reload() {
      String lang = AxTrade.CONFIG.getString("language", "en_US").toLowerCase();
      File file = new File(AxTrade.getInstance().getDataFolder(), "assets/" + lang + ".yml");
      boolean exists = file.exists();
      translations = new Config(file);
      if (!exists || translations.getBackingDocument().isEmpty(true)) {
         List<String> versions = Version.getServerVersion().getVersions();
         String version = versions.getLast();
         String url = "https://api.github.com/repos/InventivetalentDev/minecraft-assets/contents/assets/minecraft/lang/" + lang + ".json?ref=" + version;
         Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FF00╠ &#AAFFAADownloading &f" + lang + " &#AAFFAAlanguage files.."));
         HttpClient client = HttpClient.newHttpClient();
         HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

         try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            JsonObject object = (JsonObject)gson.fromJson(response.body(), JsonObject.class);
            String base64Content = object.get("content").getAsString().replace("\n", "");
            JsonObject gsonObject = (JsonObject)gson.fromJson(new String(Base64.getDecoder().decode(base64Content)), JsonObject.class);

            for (Entry<String, JsonElement> e : gsonObject.entrySet()) {
               if (e.getKey().startsWith("item.minecraft.")) {
                  String name = e.getKey().replace("item.minecraft.", "");
                  if (name.contains(".")) {
                     continue;
                  }

                  translations.set("material." + name, e.getValue().getAsString());
               }

               if (e.getKey().startsWith("block.minecraft.")) {
                  String name = e.getKey().replace("block.minecraft.", "");
                  if (!name.contains(".")) {
                     translations.set("material." + name, e.getValue().getAsString());
                  }
               }
            }

            translations.save();
         } catch (Exception var15) {
            var15.printStackTrace();
         }

         client.close();
      }
   }

   public static String getTranslated(@NotNull Material material) {
      return translations.getString("material." + material.name().toLowerCase(), material.name().toLowerCase().replace("_", " "));
   }
}
