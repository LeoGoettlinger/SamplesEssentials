package ac.grim.grimac.utils.common;

import ac.grim.grimac.utils.anticheat.LogUtil;
import java.io.InputStream;
import java.util.Properties;
import lombok.Generated;

public final class PropertiesUtil {
   public static Properties readProperties(Class<?> clazz, String path) {
      Properties properties = new Properties();

      try (InputStream inputStream = clazz.getClassLoader().getResourceAsStream(path)) {
         if (inputStream == null) {
            throw new RuntimeException("Cannot find properties file: " + path);
         }

         properties.load(inputStream);
      } catch (Exception var8) {
         LogUtil.error(var8);
      }

      return properties;
   }

   public static String getPropertyOrElse(Properties properties, String key, String defaultValue) {
      return properties.getProperty(key, defaultValue);
   }

   @Generated
   private PropertiesUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
