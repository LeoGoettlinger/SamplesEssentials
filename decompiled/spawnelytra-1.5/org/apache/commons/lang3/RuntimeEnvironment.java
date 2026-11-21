package org.apache.commons.lang3;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class RuntimeEnvironment {
   private static boolean fileExists(String path) {
      return Files.exists(Paths.get(path));
   }

   public static Boolean inContainer() {
      return inContainer("");
   }

   static boolean inContainer(String dirPrefix) {
      String value = readFile(dirPrefix + "/proc/1/environ", "container");
      return value != null ? !value.isEmpty() : fileExists(dirPrefix + "/.dockerenv") || fileExists(dirPrefix + "/run/.containerenv");
   }

   private static String readFile(String envVarFile, String key) {
      try {
         byte[] bytes = Files.readAllBytes(Paths.get(envVarFile));
         String content = new String(bytes, Charset.defaultCharset());
         String[] lines = content.split(String.valueOf('\u0000'));
         String prefix = key + "=";
         return Arrays.stream(lines)
            .filter(line -> line.startsWith(prefix))
            .map(line -> line.split("=", 2))
            .map(keyValue -> (String)keyValue[1])
            .findFirst()
            .orElse(null);
      } catch (IOException var6) {
         return null;
      }
   }
}
