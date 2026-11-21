package com.oheers.fish.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManifestUtil {
   private static final Logger logger = LoggerFactory.getLogger(ManifestUtil.class.getName());
   private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";

   private ManifestUtil() {
      throw new UnsupportedOperationException();
   }

   public static String getAttributeFromManifest(String key, String defaultValue) {
      return getAttributeFromManifest(ManifestUtil.class.getClassLoader(), key, defaultValue);
   }

   public static String getAttributeFromManifest(ClassLoader classLoader, String key, String defaultValue) {
      if (key != null && !key.isEmpty()) {
         try (InputStream inputStream = classLoader.getResourceAsStream("META-INF/MANIFEST.MF")) {
            if (inputStream != null) {
               Manifest manifest = new Manifest(inputStream);
               String value = manifest.getMainAttributes().getValue(key);
               return value != null ? value : defaultValue;
            } else {
               return defaultValue;
            }
         } catch (IOException var9) {
            logger.error("Error reading manifest file: {}", var9.getMessage());
            return defaultValue;
         }
      } else {
         throw new IllegalArgumentException("Key cannot be null or empty");
      }
   }
}
