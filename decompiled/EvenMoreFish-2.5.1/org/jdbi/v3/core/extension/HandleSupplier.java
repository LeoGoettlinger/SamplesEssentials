package org.jdbi.v3.core.extension;

import java.util.concurrent.Callable;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.config.ConfigRegistry;

public interface HandleSupplier extends AutoCloseable {
   Handle getHandle();

   Jdbi getJdbi();

   ConfigRegistry getConfig();

   <V> V invokeInContext(ExtensionContext extensionContext, Callable<V> task) throws Exception;

   @Override
   default void close() {
   }
}
