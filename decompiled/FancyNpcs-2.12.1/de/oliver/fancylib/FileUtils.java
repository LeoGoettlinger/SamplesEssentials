package de.oliver.fancylib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bukkit.plugin.Plugin;

public class FileUtils {
   public static String getSHA256Checksum(File file) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");

         try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];

            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
               digest.update(byteArray, 0, bytesCount);
            }
         }

         byte[] var12 = digest.digest();
         StringBuilder var13 = new StringBuilder();

         for (byte hashByte : var12) {
            String hex = Integer.toHexString(255 & hashByte);
            if (hex.length() == 1) {
               var13.append('0');
            }

            var13.append(hex);
         }

         return var13.toString();
      } catch (NoSuchAlgorithmException | IOException var11) {
         return "N/A";
      }
   }

   public static File findFirstFileByName(File directory, String name) {
      File[] files = directory.listFiles();
      if (files == null) {
         return null;
      } else {
         for (File file : files) {
            if (file.getName().toLowerCase().contains(name.toLowerCase())) {
               return file;
            }
         }

         return null;
      }
   }

   public String readResource(String name) {
      URL url = this.getClass().getClassLoader().getResource(name);
      if (url == null) {
         return null;
      } else {
         URLConnection connection = null;

         try {
            connection = url.openConnection();
         } catch (IOException var8) {
            var8.printStackTrace();
         }

         connection.setUseCaches(false);

         try {
            String var6;
            try (InputStream inputStream = connection.getInputStream()) {
               byte[] file_raw = new byte[inputStream.available()];
               inputStream.read(file_raw);
               inputStream.close();
               var6 = new String(file_raw, StandardCharsets.UTF_8);
            }

            return var6;
         } catch (IOException var10) {
            var10.printStackTrace();
            return null;
         }
      }
   }

   public void saveFile(Plugin plugin, String name) {
      URL url = this.getClass().getClassLoader().getResource(name);
      if (url != null) {
         File file = new File(plugin.getDataFolder() + File.separator + name);
         if (!file.exists()) {
            if (!file.getParentFile().exists()) {
               file.getParentFile().mkdirs();
            }

            URLConnection connection = null;

            try {
               connection = url.openConnection();
            } catch (IOException var11) {
               var11.printStackTrace();
            }

            connection.setUseCaches(false);

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
               InputStream inputStream = connection.getInputStream();
               byte[] bytes = new byte[1024];

               int read;
               while ((read = inputStream.read(bytes)) != -1) {
                  outputStream.write(bytes, 0, read);
               }
            } catch (IOException var13) {
               var13.printStackTrace();
            }
         }
      }
   }
}
