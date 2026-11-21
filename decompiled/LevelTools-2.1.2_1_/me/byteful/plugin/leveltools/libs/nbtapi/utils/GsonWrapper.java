package me.byteful.plugin.leveltools.libs.nbtapi.utils;

import com.google.gson.Gson;
import me.byteful.plugin.leveltools.libs.nbtapi.NbtApiException;

@Deprecated
public class GsonWrapper {
   private static Gson gson = new Gson();

   private GsonWrapper() {
   }

   public static String getString(Object obj) {
      return gson.toJson(obj);
   }

   public static void overwriteGsonInstance(Gson replacement) {
      gson = replacement;
   }

   public static <T> T deserializeJson(String json, Class<T> type) {
      try {
         if (json == null) {
            return null;
         } else {
            T obj = (T)gson.fromJson(json, type);
            return type.cast(obj);
         }
      } catch (Exception var3) {
         throw new NbtApiException("Error while converting json to " + type.getName(), var3);
      }
   }
}
