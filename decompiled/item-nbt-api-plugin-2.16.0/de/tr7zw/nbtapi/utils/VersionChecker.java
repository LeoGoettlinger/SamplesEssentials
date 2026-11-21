package de.tr7zw.nbtapi.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.tr7zw.nbtapi.NBTItem;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

public class VersionChecker {
   private static final String USER_AGENT = "nbt-api Version check";
   private static final String REQUEST_URL = "https://api.spiget.org/v2/resources/7939/versions?size=100";
   public static boolean hideOk = false;

   protected static void checkForUpdates() throws Exception {
      URL url = new URL("https://api.spiget.org/v2/resources/7939/versions?size=100");
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      connection.addRequestProperty("User-Agent", "nbt-api Version check");
      InputStream inputStream = connection.getInputStream();
      InputStreamReader reader = new InputStreamReader(inputStream);
      JsonElement element = new JsonParser().parse(reader);
      if (element.isJsonArray()) {
         JsonArray updates = (JsonArray)element;
         JsonObject latest = (JsonObject)updates.get(updates.size() - 1);
         int versionDifference = getVersionDifference(latest.get("name").getAsString());
         if (versionDifference == -1) {
            MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] The NBT-API in '" + getPlugin() + "' seems to be outdated!");
            MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Current Version: '2.16.0' Newest Version: " + latest.get("name").getAsString() + "'");
            MinecraftVersion.getLogger()
               .log(
                  Level.WARNING,
                  "[NBTAPI] Please update the NBTAPI or the plugin that contains the api(nag the mod author when the newest release has an old version, not the NBTAPI dev)!"
               );
         } else if (versionDifference == 0) {
            if (!hideOk) {
               MinecraftVersion.getLogger().log(Level.INFO, "[NBTAPI] The NBT-API seems to be up-to-date!");
            }
         } else if (versionDifference == 1) {
            MinecraftVersion.getLogger()
               .log(
                  Level.INFO,
                  "[NBTAPI] The NBT-API in '" + getPlugin() + "' seems to be a future Version, not yet released on Spigot/CurseForge! This is not an error!"
               );
            MinecraftVersion.getLogger().log(Level.INFO, "[NBTAPI] Current Version: '2.16.0' Newest Version: " + latest.get("name").getAsString() + "'");
         }
      } else {
         MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error when looking for Updates! Got non Json Array: '" + element.toString() + "'");
      }
   }

   private static int getVersionDifference(String version) {
      String current = "2.16.0";
      if (current.equals(version)) {
         return 0;
      } else {
         String pattern = "\\.";
         if (current.split(pattern).length == 3 && version.split(pattern).length == 3) {
            int curMaj = Integer.parseInt(current.split(pattern)[0]);
            int curMin = Integer.parseInt(current.split(pattern)[1]);
            String curPatch = current.split(pattern)[2];
            int relMaj = Integer.parseInt(version.split(pattern)[0]);
            int relMin = Integer.parseInt(version.split(pattern)[1]);
            String relPatch = version.split(pattern)[2];
            if (curMaj < relMaj) {
               return -1;
            } else if (curMaj > relMaj) {
               return 1;
            } else if (curMin < relMin) {
               return -1;
            } else if (curMin > relMin) {
               return 1;
            } else {
               int curPatchN = Integer.parseInt(curPatch.split("-")[0]);
               int relPatchN = Integer.parseInt(relPatch.split("-")[0]);
               if (curPatchN < relPatchN) {
                  return -1;
               } else if (curPatchN > relPatchN) {
                  return 1;
               } else if (!relPatch.contains("-") && curPatch.contains("-")) {
                  return -1;
               } else {
                  return relPatch.contains("-") && curPatch.contains("-") ? 0 : 1;
               }
            }
         } else {
            return -1;
         }
      }
   }

   protected static String getPlugin() {
      ClassLoader classLoader = VersionChecker.class.getClassLoader();
      InputStream inputStream = classLoader.getResourceAsStream("paper-plugin.yml");
      if (inputStream != null) {
         try {
            InputStreamReader reader = new InputStreamReader(inputStream);

            String var16;
            try {
               YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration(reader);
               var16 = pluginYml.getString("name");
            } catch (Throwable var10) {
               try {
                  reader.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            reader.close();
            return var16;
         } catch (IOException var11) {
         } catch (IllegalArgumentException var12) {
            MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error reading paper-plugin.yml: " + var12.getMessage());
         }
      }

      inputStream = classLoader.getResourceAsStream("plugin.yml");
      if (inputStream != null) {
         try {
            InputStreamReader reader = new InputStreamReader(inputStream);

            String var4;
            try {
               YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration(reader);
               var4 = pluginYml.getString("name");
            } catch (Throwable var6) {
               try {
                  reader.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }

               throw var6;
            }

            reader.close();
            return var4;
         } catch (IOException var7) {
         } catch (IllegalArgumentException var8) {
            MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error reading plugin.yml: " + var8.getMessage());
         }
      }

      return NBTItem.class.getPackage().getName();
   }

   protected static String getPluginforBStats() {
      ClassLoader classLoader = VersionChecker.class.getClassLoader();
      InputStream inputStream = classLoader.getResourceAsStream("paper-plugin.yml");
      if (inputStream != null) {
         try {
            InputStreamReader reader = new InputStreamReader(inputStream);

            String var16;
            try {
               YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration(reader);
               var16 = pluginYml.getString("name");
            } catch (Throwable var10) {
               try {
                  reader.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            reader.close();
            return var16;
         } catch (IOException var11) {
         } catch (IllegalArgumentException var12) {
            MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error reading paper-plugin.yml: " + var12.getMessage());
         }
      }

      inputStream = classLoader.getResourceAsStream("plugin.yml");
      if (inputStream != null) {
         try {
            InputStreamReader reader = new InputStreamReader(inputStream);

            String var4;
            try {
               YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration(reader);
               var4 = pluginYml.getString("name");
            } catch (Throwable var6) {
               try {
                  reader.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }

               throw var6;
            }

            reader.close();
            return var4;
         } catch (IOException var7) {
         } catch (IllegalArgumentException var8) {
            MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error reading plugin.yml: " + var8.getMessage());
         }
      }

      return "UnknownPlugin";
   }

   protected static String getPluginType() {
      ClassLoader classLoader = VersionChecker.class.getClassLoader();
      InputStream inputStream = classLoader.getResourceAsStream("paper-plugin.yml");
      if (inputStream != null) {
         try {
            inputStream.close();
         } catch (IOException var3) {
         }

         return "PaperPlugin";
      } else {
         inputStream = classLoader.getResourceAsStream("plugin.yml");
         if (inputStream != null) {
            try {
               inputStream.close();
            } catch (IOException var4) {
            }

            return "SpigotPlugin";
         } else {
            return "UnknownPlugin";
         }
      }
   }
}
