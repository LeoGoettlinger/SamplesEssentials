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
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGEnvironment;
import org.postgresql.PGProperty;
import org.postgresql.util.OSUtil;
import org.postgresql.util.PGPropertyUtil;

public class PgServiceConfParser {
   private static final Logger LOGGER = Logger.getLogger(PgServiceConfParser.class.getName());
   private final String serviceName;
   private boolean ignoreIfOpenFails = true;

   private PgServiceConfParser(String serviceName) {
      this.serviceName = serviceName;
   }

   @Nullable
   public static Properties getServiceProperties(String serviceName) {
      PgServiceConfParser pgServiceConfParser = new PgServiceConfParser(serviceName);
      return pgServiceConfParser.findServiceDescription();
   }

   @Nullable
   private Properties findServiceDescription() {
      String resourceName = this.findPgServiceConfResourceName();
      if (resourceName == null) {
         return null;
      } else {
         Properties result = null;

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
            Level level = this.ignoreIfOpenFails ? Level.FINE : Level.WARNING;
            LOGGER.log(level, "Failed to handle resource [{0}] with error [{1}]", new Object[]{resourceName, var8.getMessage()});
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
   private String findPgServiceConfResourceName() {
      String pgServiceConfFileDefaultName = PGEnvironment.PGSERVICEFILE.getDefaultValue();
      String propertyName = PGEnvironment.ORG_POSTGRESQL_PGSERVICEFILE.getName();
      String resourceName = System.getProperty(propertyName);
      if (resourceName != null && !resourceName.trim().isEmpty()) {
         this.ignoreIfOpenFails = false;
         LOGGER.log(Level.FINE, "Value [{0}] selected from property [{1}]", new Object[]{resourceName, propertyName});
         return resourceName;
      } else {
         propertyName = PGEnvironment.PGSERVICEFILE.getName();
         resourceName = System.getenv().get(propertyName);
         if (resourceName != null && !resourceName.trim().isEmpty()) {
            this.ignoreIfOpenFails = false;
            LOGGER.log(Level.FINE, "Value [{0}] selected from environment variable [{1}]", new Object[]{resourceName, propertyName});
            return resourceName;
         } else {
            propertyName = "." + pgServiceConfFileDefaultName;
            File resourceFile = new File(OSUtil.getUserConfigRootDirectory(), propertyName);
            if (resourceFile.canRead()) {
               LOGGER.log(Level.FINE, "Value [{0}] selected because file exist in user home directory", new Object[]{resourceFile.getAbsolutePath()});
               return resourceFile.getAbsolutePath();
            } else {
               propertyName = PGEnvironment.PGSYSCONFDIR.getName();
               resourceName = System.getenv().get(propertyName);
               if (resourceName != null && !resourceName.trim().isEmpty()) {
                  String resourceNamex = resourceName + File.separator + pgServiceConfFileDefaultName;
                  LOGGER.log(Level.FINE, "Value [{0}] selected using environment variable [{1}]", new Object[]{resourceNamex, propertyName});
                  return resourceNamex;
               } else {
                  LOGGER.log(Level.FINE, "Value for resource [{0}] not found", pgServiceConfFileDefaultName);
                  return null;
               }
            }
         }
      }
   }

   @Nullable
   private Properties parseInputStream(InputStream inputStream) throws IOException {
      Set<String> allowedServiceKeys = Arrays.stream(PGProperty.values())
         .map(PGProperty::getName)
         .map(PGPropertyUtil::translatePGPropertyToPGService)
         .collect(Collectors.toSet());
      Properties result = new Properties();
      boolean isFound = false;
      Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

      Object var14;
      label96: {
         Object var19;
         label100: {
            try {
               BufferedReader br;
               label92: {
                  label101: {
                     br = new BufferedReader(reader);

                     try {
                        int lineNumber = 0;

                        while (true) {
                           String originalLine;
                           if ((originalLine = br.readLine()) == null) {
                              break label92;
                           }

                           lineNumber++;
                           String line = originalLine.trim();
                           if (!line.isEmpty() && !line.startsWith("#")) {
                              int indexOfEqualSign = line.indexOf("=");
                              if (line.startsWith("[") && line.endsWith("]")) {
                                 if (isFound) {
                                    break label92;
                                 }

                                 String sectionName = line.substring(1, line.length() - 1);
                                 if (this.serviceName.equals(sectionName)) {
                                    isFound = true;
                                 }
                              } else if (isFound) {
                                 if (indexOfEqualSign <= 1) {
                                    LOGGER.log(Level.WARNING, "Not valid line: line number [{0}], value [{1}]", new Object[]{lineNumber, originalLine});
                                    var19 = null;
                                    break;
                                 }

                                 String key = line.substring(0, indexOfEqualSign);
                                 String value = line.substring(indexOfEqualSign + 1);
                                 if (!allowedServiceKeys.contains(key)) {
                                    String allowedValuesCommaSeparated = allowedServiceKeys.stream().sorted().collect(Collectors.joining(","));
                                    LOGGER.log(
                                       Level.SEVERE,
                                       "Got invalid key: line number [{0}], value [{1}], allowed values [{2}]",
                                       new Object[]{lineNumber, originalLine, allowedValuesCommaSeparated}
                                    );
                                    var14 = null;
                                    break label101;
                                 }

                                 if (!value.isEmpty()) {
                                    result.putIfAbsent(PGPropertyUtil.translatePGServiceToPGProperty(key), value);
                                 }
                              }
                           }
                        }
                     } catch (Throwable var17) {
                        try {
                           br.close();
                        } catch (Throwable var16) {
                           var17.addSuppressed(var16);
                        }

                        throw var17;
                     }

                     br.close();
                     break label100;
                  }

                  br.close();
                  break label96;
               }

               br.close();
            } catch (Throwable var18) {
               try {
                  reader.close();
               } catch (Throwable var15) {
                  var18.addSuppressed(var15);
               }

               throw var18;
            }

            reader.close();
            return isFound ? result : null;
         }

         reader.close();
         return (Properties)var19;
      }

      reader.close();
      return (Properties)var14;
   }
}
