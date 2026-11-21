package org.jdbi.v3.core.mapper.reflect.internal;

import java.lang.reflect.Type;
import org.jdbi.v3.core.config.ConfigRegistry;

@FunctionalInterface
public interface PojoPropertiesFactory {
   PojoProperties<?> create(Type type, ConfigRegistry config);
}
