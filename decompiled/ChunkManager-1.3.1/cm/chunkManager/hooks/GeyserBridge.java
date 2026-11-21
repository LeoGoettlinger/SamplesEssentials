package cm.chunkManager.hooks;

import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Bukkit;

public class GeyserBridge {
   private static Boolean available;
   private static Method isBedrockMethod;
   private static Object apiInstance;

   public static boolean isAvailable() {
      if (available == null) {
         available = Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null;
      }

      return available;
   }

   public static boolean isBedrockPlayer(UUID uuid) {
      if (!isAvailable()) {
         return false;
      } else {
         try {
            if (apiInstance == null) {
               Class<?> apiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
               Method apiMethod = apiClass.getMethod("api");
               apiInstance = apiMethod.invoke(null);
               isBedrockMethod = apiInstance.getClass().getMethod("isBedrockPlayer", UUID.class);
            }

            return (Boolean)isBedrockMethod.invoke(apiInstance, uuid);
         } catch (Exception | NoClassDefFoundError var3) {
            return false;
         }
      }
   }
}
