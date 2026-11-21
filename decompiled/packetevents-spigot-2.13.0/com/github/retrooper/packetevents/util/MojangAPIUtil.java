package com.github.retrooper.packetevents.util;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MojangAPIUtil {
   public static List<TextureProperty> requestPlayerTextureProperties(UUID uuid) {
      String uuidStr = UUIDUtil.toStringWithoutDashes(uuid);

      try {
         List<TextureProperty> textureProperties = new ArrayList<>();
         URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidStr + "?unsigned=false");
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");
         if (connection.getResponseCode() != 200) {
            throw new IllegalStateException(
               "Failed to request texture properties with their UUID " + uuidStr + "! Response code: " + connection.getResponseCode()
            );
         } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
               sb.append(inputLine);
            }

            in.close();
            JsonObject responseObject = (JsonObject)AdventureSerializer.serializer().gson().serializer().fromJson(sb.toString(), JsonObject.class);

            for (JsonElement element : responseObject.get("properties").getAsJsonArray()) {
               JsonObject property = element.getAsJsonObject();
               String name = property.get("name").getAsString();
               String value = property.get("value").getAsString();
               String signature = null;
               if (property.has("signature")) {
                  signature = property.get("signature").getAsString();
               }

               textureProperties.add(new TextureProperty(name, value, signature));
            }

            return textureProperties;
         }
      } catch (IOException var16) {
         var16.printStackTrace();
         return null;
      }
   }

   public static String requestPlayerName(UUID uuid) {
      String uuidStr = UUIDUtil.toStringWithoutDashes(uuid);

      try {
         URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidStr);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");
         if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Failed to request player name with their UUID " + uuidStr + "! Response code: " + connection.getResponseCode());
         } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
               sb.append(inputLine);
            }

            in.close();
            JsonObject responseObject = (JsonObject)AdventureSerializer.serializer().gson().serializer().fromJson(sb.toString(), JsonObject.class);
            return responseObject.get("name").getAsString();
         }
      } catch (IOException var8) {
         var8.printStackTrace();
         return null;
      }
   }

   public static UUID requestPlayerUUID(String name) {
      try {
         URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");
         if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Failed to request player UUID with their name: " + name + "! Response code: " + connection.getResponseCode());
         } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
               sb.append(inputLine);
            }

            in.close();
            JsonObject responseObject = (JsonObject)AdventureSerializer.serializer().gson().serializer().fromJson(sb.toString(), JsonObject.class);
            String uuidStr = responseObject.get("id").getAsString();
            return UUIDUtil.fromStringWithoutDashes(uuidStr);
         }
      } catch (IOException var8) {
         var8.printStackTrace();
         return null;
      }
   }
}
