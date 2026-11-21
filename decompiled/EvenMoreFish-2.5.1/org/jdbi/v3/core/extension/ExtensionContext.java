package org.jdbi.v3.core.extension;

import java.lang.reflect.Method;
import org.jdbi.v3.core.config.ConfigRegistry;

public final class ExtensionContext {
   private final ConfigRegistry config;
   private final ExtensionMethod extensionMethod;

   public static ExtensionContext forConfig(ConfigRegistry config) {
      return new ExtensionContext(config, null);
   }

   public static ExtensionContext forExtensionMethod(ConfigRegistry config, Class<?> type, Method method) {
      return new ExtensionContext(config, new ExtensionMethod(type, method));
   }

   public ExtensionContext(ConfigRegistry config, ExtensionMethod extensionMethod) {
      this.config = config;
      this.extensionMethod = extensionMethod;
   }

   public ConfigRegistry getConfig() {
      return this.config;
   }

   public ExtensionMethod getExtensionMethod() {
      return this.extensionMethod;
   }
}
