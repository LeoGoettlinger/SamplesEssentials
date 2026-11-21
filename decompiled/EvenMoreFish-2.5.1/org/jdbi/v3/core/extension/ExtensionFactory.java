package org.jdbi.v3.core.extension;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.meta.Alpha;

@FunctionalInterface
public interface ExtensionFactory {
   boolean accepts(Class<?> extensionType);

   default <E> E attach(Class<E> extensionType, HandleSupplier handleSupplier) {
      throw new UnsupportedOperationException("Virtual factories do not support attach()");
   }

   @Alpha
   default Collection<ExtensionHandlerFactory> getExtensionHandlerFactories(ConfigRegistry config) {
      return Collections.emptySet();
   }

   @Alpha
   default Collection<ExtensionHandlerCustomizer> getExtensionHandlerCustomizers(ConfigRegistry config) {
      return Collections.emptySet();
   }

   @Alpha
   default Collection<ConfigCustomizerFactory> getConfigCustomizerFactories(ConfigRegistry config) {
      return Collections.emptySet();
   }

   @Alpha
   default void buildExtensionMetadata(ExtensionMetadata.Builder builder) {
   }

   @Alpha
   default Set<ExtensionFactory.FactoryFlag> getFactoryFlags() {
      return Collections.emptySet();
   }

   @Alpha
   public static enum FactoryFlag {
      NON_VIRTUAL_FACTORY,
      DONT_USE_PROXY;
   }
}
