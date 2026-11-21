package org.postgresql.jdbcurlresolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGEnvironment;
import org.postgresql.util.OSUtil;

public class PgPassParser {
   private static final Logger LOGGER = Logger.getLogger(PgPassParser.class.getName());
   private static final char SEPARATOR = ':';
   private final String hostname;
   private final String port;
   private final String database;
   private final String user;

   private PgPassParser(String hostname, String port, String database, String user) {
      this.hostname = hostname;
      this.port = port;
      this.database = database;
      this.user = user;
   }

   @Nullable
   public static String getPassword(@Nullable String hostname, @Nullable String port, @Nullable String database, @Nullable String user) {
      if (hostname == null || hostname.isEmpty()) {
         return null;
      } else if (port == null || port.isEmpty()) {
         return null;
      } else if (database == null || database.isEmpty()) {
         return null;
      } else if (user != null && !user.isEmpty()) {
         PgPassParser pgPassParser = new PgPassParser(hostname, port, database, user);
         return pgPassParser.findPassword();
      } else {
         return null;
      }
   }

   @Nullable
   private String findPassword() {
      String resourceName = this.findPgPasswordResourceName();
      if (resourceName == null) {
         return null;
      } else {
         String result = null;

         try {
            InputStream inputStream = this.openInputStream(resourceName);

            try {
               result = this.parseInputStream(inputStream);
            } catch (Throwable var7) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (inputStream != null) {
               inputStream.close();
            }
         } catch (IOException var8) {
            LOGGER.log(Level.FINE, "Failed to handle resource [{0}] with error [{1}]", new Object[]{resourceName, var8.getMessage()});
         }

         return result;
      }
   }

   private InputStream openInputStream(String resourceName) throws IOException {
      try {
         URL url = new URL(resourceName);
         return url.openStream();
      } catch (MalformedURLException var4) {
         File file = new File(resourceName);
         return new FileInputStream(file);
      }
   }

   @Nullable
   private String findPgPasswordResourceName() {
      String pgPassFileDefaultName = PGEnvironment.PGPASSFILE.getDefaultValue();
      String propertyName = PGEnvironment.ORG_POSTGRESQL_PGPASSFILE.getName();
      String resourceName = System.getProperty(propertyName);
      if (resourceName != null && !resourceName.trim().isEmpty()) {
         LOGGER.log(Level.FINE, "Value [{0}] selected from property [{1}]", new Object[]{resourceName, propertyName});
         return resourceName;
      } else {
         propertyName = PGEnvironment.PGPASSFILE.getName();
         resourceName = System.getenv().get(propertyName);
         if (resourceName != null && !resourceName.trim().isEmpty()) {
            LOGGER.log(Level.FINE, "Value [{0}] selected from environment variable [{1}]", new Object[]{resourceName, propertyName});
            return resourceName;
         } else {
            propertyName = "";
            if (!OSUtil.isWindows()) {
               propertyName = propertyName + ".";
            }

            propertyName = propertyName + pgPassFileDefaultName;
            if (OSUtil.isWindows()) {
               propertyName = propertyName + ".conf";
            }

            File resourceFile = new File(OSUtil.getUserConfigRootDirectory(), propertyName);
            if (resourceFile.canRead()) {
               LOGGER.log(Level.FINE, "Value [{0}] selected because file exist in user home directory", new Object[]{resourceFile.getAbsolutePath()});
               return resourceFile.getAbsolutePath();
            } else {
               LOGGER.log(Level.FINE, "Value for resource [{0}] not found", pgPassFileDefaultName);
               return null;
            }
         }
      }
   }

   @Nullable
   private String parseInputStream(InputStream inputStream) throws IOException {
      String result = null;
      Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

      try {
         BufferedReader br = new BufferedReader(reader);

         try {
            int currentLine = 0;

            String line;
            while ((line = br.readLine()) != null) {
               currentLine++;
               if (!line.trim().isEmpty() && !line.startsWith("#")) {
                  result = this.evaluateLine(line, currentLine);
                  if (result != null) {
                     break;
                  }
               }
            }
         } catch (Throwable var9) {
            try {
               br.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         br.close();
      } catch (Throwable var10) {
         try {
            reader.close();
         } catch (Throwable var7) {
            var10.addSuppressed(var7);
         }

         throw var10;
      }

      reader.close();
      return result;
   }

   @Nullable
   private String evaluateLine(String fullLine, int currentLine) {
      String result = null;
      String line;
      if ((line = this.checkForPattern(fullLine, this.hostname)) != null
         && (line = this.checkForPattern(line, this.port)) != null
         && (line = this.checkForPattern(line, this.database)) != null
         && (line = this.checkForPattern(line, this.user)) != null) {
         result = this.extractPassword(line);
         String lineWithoutPassword = fullLine.substring(0, fullLine.length() - line.length());
         LOGGER.log(
            Level.FINE,
            "Matching line number [{0}] with value prefix [{1}] found for input [{2}:{3}:{4}:{5}]",
            new Object[]{currentLine, lineWithoutPassword, this.hostname, this.port, this.database, this.user}
         );
      }

      return result;
   }

   private String extractPassword(String line) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < line.length(); i++) {
         char chr = line.charAt(i);
         if (chr == '\\' && i + 1 < line.length()) {
            char nextChr = line.charAt(i + 1);
            if (nextChr == '\\' || nextChr == ':') {
               chr = nextChr;
               i++;
            }
         } else if (chr == ':') {
            break;
         }

         sb.append(chr);
      }

      return sb.toString();
   }

   @Nullable
   private String checkForPattern(String line, String value) {
      String result = null;
      if (line.startsWith("*:")) {
         result = line.substring(2);
      } else {
         int lPos = 0;

         for (int vPos = 0; vPos < value.length(); vPos++) {
            if (lPos >= line.length()) {
               return null;
            }

            char l = line.charAt(lPos);
            if (l == '\\') {
               if (lPos + 1 >= line.length()) {
                  return null;
               }

               char next = line.charAt(lPos + 1);
               if (next == '\\' || next == ':') {
                  l = next;
                  lPos++;
               }
            }

            lPos++;
            char v = value.charAt(vPos);
            if (l != v) {
               return null;
            }
         }

         if (line.charAt(lPos) == ':') {
            result = line.substring(lPos + 1);
         }
      }

      return result;
   }
}
