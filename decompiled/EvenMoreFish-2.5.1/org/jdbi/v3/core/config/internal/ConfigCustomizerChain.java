package org.jdbi.v3.core.config.internal;

import java.util.LinkedHashSet;
import java.util.Set;
import org.jdbi.v3.core.config.ConfigCustomizer;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.meta.Beta;

@Beta
public final class ConfigCustomizerChain implements ConfigCustomizer {
   private final Set<ConfigCustomizer> configCustomizers = new LinkedHashSet<>();

   public void addCustomizer(final ConfigCustomizer configCustomizer) {
      this.configCustomizers.add(configCustomizer);
   }

   @Override
   public void customize(final ConfigRegistry config) {
      this.configCustomizers.forEach(configCustomizer -> configCustomizer.customize(config));
   }
}
