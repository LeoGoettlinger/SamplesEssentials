package org.jdbi.v3.core.extension;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import org.jdbi.v3.core.config.ConfigCustomizer;
import org.jdbi.v3.meta.Alpha;

@Alpha
public interface ConfigCustomizerFactory {
   default Collection<ConfigCustomizer> forExtensionType(Class<?> extensionType) {
      return Collections.emptyList();
   }

   default Collection<ConfigCustomizer> forExtensionMethod(Class<?> extensionType, Method method) {
      return Collections.emptyList();
   }
}
