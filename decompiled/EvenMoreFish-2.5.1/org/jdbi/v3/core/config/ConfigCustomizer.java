package org.jdbi.v3.core.config;

import org.jdbi.v3.meta.Beta;

@FunctionalInterface
@Beta
public interface ConfigCustomizer {
   void customize(ConfigRegistry config);
}
