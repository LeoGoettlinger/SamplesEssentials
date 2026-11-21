package com.ranull.graves.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class Base64Util {
   private Base64Util() {
   }

   public static String objectToBase64(Object object) {
      try {
         String var3;
         try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);

            try {
               boos.writeObject(object);
               boos.flush();
               var3 = Base64.getEncoder().encodeToString(baos.toByteArray());
            } catch (Throwable var7) {
               try {
                  boos.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            boos.close();
         }

         return var3;
      } catch (IOException var9) {
         return null;
      }
   }

   public static Object base64ToObject(String string) {
      byte[] data;
      try {
         data = Base64.getDecoder().decode(string);
      } catch (IllegalArgumentException var8) {
         return null;
      }

      try {
         BukkitObjectInputStream bois = new BukkitObjectInputStream(new ByteArrayInputStream(data));

         Object var3;
         try {
            var3 = bois.readObject();
         } catch (Throwable var6) {
            try {
               bois.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         bois.close();
         return var3;
      } catch (ClassNotFoundException | IOException var7) {
         return null;
      }
   }

   public static URL extractSkinURL(String base64) {
      if (base64 != null && !base64.isEmpty()) {
         try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            String json = new String(decoded);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("textures")) {
               JsonObject textures = root.getAsJsonObject("textures");
               if (textures.has("SKIN")) {
                  JsonObject skin = textures.getAsJsonObject("SKIN");
                  if (skin.has("url")) {
                     String urlString = skin.get("url").getAsString();

                     try {
                        return new URL(urlString);
                     } catch (MalformedURLException var8) {
                        return null;
                     }
                  }
               }
            }
         } catch (IllegalStateException | IllegalArgumentException var9) {
         }

         return null;
      } else {
         return null;
      }
   }
}
