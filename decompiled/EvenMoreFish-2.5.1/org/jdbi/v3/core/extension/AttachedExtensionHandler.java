package org.jdbi.v3.core.extension;

import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.meta.Alpha;
import org.jdbi.v3.meta.Beta;

@FunctionalInterface
@Alpha
public interface AttachedExtensionHandler {
   Object invoke(HandleSupplier handleSupplier, Object... args) throws Exception;

   @Beta
   default void warm(ConfigRegistry config) {
   }
}
