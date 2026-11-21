package org.jdbi.v3.sqlobject;

import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.AttachedExtensionHandler;
import org.jdbi.v3.core.extension.ExtensionHandler;
import org.jdbi.v3.core.extension.HandleSupplier;
import org.jdbi.v3.core.internal.JdbiClassUtils;

@FunctionalInterface
@Deprecated(
   since = "3.38.0",
   forRemoval = true
)
public interface Handler extends ExtensionHandler {
   Object invoke(Object target, Object[] args, HandleSupplier handleSupplier) throws Exception;

   @Override
   default AttachedExtensionHandler attachTo(ConfigRegistry config, Object target) {
      return (handleSupplier, args) -> this.invoke(target, JdbiClassUtils.safeVarargs(args), handleSupplier);
   }
}
