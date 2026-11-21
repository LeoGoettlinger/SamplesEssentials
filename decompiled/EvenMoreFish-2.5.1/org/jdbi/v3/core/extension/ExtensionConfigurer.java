package org.jdbi.v3.core.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.meta.Alpha;

@Alpha
public interface ExtensionConfigurer {
   default void configureForType(ConfigRegistry config, Annotation annotation, Class<?> extensionType) {
      throw new UnsupportedOperationException("Not supported for type");
   }

   default void configureForMethod(ConfigRegistry config, Annotation annotation, Class<?> extensionType, Method method) {
      throw new UnsupportedOperationException("Not supported for method");
   }
}
