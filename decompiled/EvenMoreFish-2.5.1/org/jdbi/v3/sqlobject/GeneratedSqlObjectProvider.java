package org.jdbi.v3.sqlobject;

import java.lang.reflect.Method;
import java.util.Collection;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.ExtensionMetadata;
import org.jdbi.v3.core.extension.HandleSupplier;
import org.jdbi.v3.meta.Alpha;

@Alpha
public interface GeneratedSqlObjectProvider {
   Class<?> extensionType();

   Collection<Method> extensionMethods();

   Object createInstance(ExtensionMetadata extensionMetadata, HandleSupplier handleSupplier, ConfigRegistry config);

   Object createOnDemand(Jdbi jdbi);
}
