package de.oliver.fancylib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.oliver.fancyanalytics.logger.ExtendedFancyLogger;
import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UUIDFetcher {
   private static final ExtendedFancyLogger LOGGER = new ExtendedFancyLogger("UUIDFetcher");
   private static final String UUID_URL = "https://api.minecraftservices.com/minecraft/profile/lookup/name/%s";
   private static final String NAME_URL = "https://api.minecraftservices.com/minecraft/profile/lookup/%s";
   private static Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
   private static Map<String, UUID> uuidCache = new HashMap<>();
   private static Map<UUID, String> nameCache = new HashMap<>();
   private static ExecutorService pool = Executors.newCachedThreadPool();
   private String name;
   private UUID id;

   public static void getUUID(String name, Consumer<UUID> action) {
      pool.execute(() -> action.accept(getUUID(name)));
   }

   public static UUID getUUID(String name) {
      name = name.toLowerCase();
      if (uuidCache.containsKey(name)) {
         return uuidCache.get(name);
      } else {
         try {
            HttpURLConnection connection = (HttpURLConnection)new URL(String.format("https://api.minecraftservices.com/minecraft/profile/lookup/name/%s", name))
               .openConnection();
            connection.setReadTimeout(5000);
            UUIDFetcher data = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher.class);
            uuidCache.put(name, data.id);
            nameCache.put(data.id, data.name);
            return data.id;
         } catch (Exception var3) {
            LOGGER.error("Could not fetch UUID for name: " + name, ThrowableProperty.of(var3));
            return null;
         }
      }
   }

   public static void getName(UUID uuid, Consumer<String> action) {
      pool.execute(() -> action.accept(getName(uuid)));
   }

   public static String getName(UUID uuid) {
      if (nameCache.containsKey(uuid)) {
         return nameCache.get(uuid);
      } else {
         try {
            HttpURLConnection connection = (HttpURLConnection)new URL(
                  String.format("https://api.minecraftservices.com/minecraft/profile/lookup/%s", UUIDTypeAdapter.fromUUID(uuid))
               )
               .openConnection();
            connection.setReadTimeout(5000);
            UUIDFetcher currentNameData = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher.class);
            uuidCache.put(currentNameData.name.toLowerCase(), uuid);
            nameCache.put(uuid, currentNameData.name);
            return currentNameData.name;
         } catch (Exception var3) {
            LOGGER.error("Could not fetch name for uuid: " + uuid, ThrowableProperty.of(var3));
            return null;
         }
      }
   }
}
