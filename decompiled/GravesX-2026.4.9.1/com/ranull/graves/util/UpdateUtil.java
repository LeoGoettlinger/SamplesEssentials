package com.ranull.graves.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UpdateUtil {
   private UpdateUtil() {
   }

   public static String getLatestVersion(int resourceId) {
      long timestamp = System.currentTimeMillis();
      String urlString = "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId + "&t=" + timestamp;

      try {
         String var6;
         try (
            InputStream inputStream = new URL(urlString).openStream();
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
         ) {
            if (!scanner.hasNext()) {
               return null;
            }

            var6 = scanner.next().trim();
         }

         return var6;
      } catch (IOException var12) {
         return null;
      }
   }
}
