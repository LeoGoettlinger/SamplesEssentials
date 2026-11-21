package com.earth2me.essentials.libs.configurate.loader;

import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.ScopedConfigurationNode;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public abstract class AbstractConfigurationFormat<N extends ScopedConfigurationNode<N>, L extends AbstractConfigurationLoader<N>, B extends AbstractConfigurationLoader.Builder<B, L>>
   implements ConfigurationFormat {
   private final String id;
   private final Supplier<B> builderMaker;
   private final Set<String> supportedExtensions;

   protected AbstractConfigurationFormat(final String id, final Supplier<B> builderMaker, final Set<String> supportedExtensions) {
      this.id = Objects.requireNonNull(id, "id");
      this.builderMaker = Objects.requireNonNull(builderMaker, "builderMaker");
      this.supportedExtensions = UnmodifiableCollections.copyOf(Objects.requireNonNull(supportedExtensions, "supportedExtensions"));
   }

   @Override
   public String id() {
      return this.id;
   }

   @Override
   public Set<String> supportedExtensions() {
      return this.supportedExtensions;
   }

   @Override
   public ConfigurationLoader<? extends Object> create(final Path file) {
      return this.builderMaker.get().path(file).build();
   }

   @Override
   public ConfigurationLoader<? extends Object> create(final Path file, final ConfigurationNode options) {
      return this.builderMaker.get().editOptions(opts -> opts.values(ValueSources.node(options))).path(file).build();
   }

   @Override
   public ConfigurationLoader<? extends Object> create(final URL url) {
      return this.builderMaker.get().url(url).build();
   }

   @Override
   public ConfigurationLoader<? extends Object> create(final URL url, final ConfigurationNode options) {
      return this.builderMaker.get().editOptions(opts -> opts.values(ValueSources.node(options))).url(url).build();
   }
}
