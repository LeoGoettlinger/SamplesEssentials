package org.jdbi.v3.core.extension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.internal.CopyOnWriteHashMap;
import org.jdbi.v3.meta.Alpha;
import org.jdbi.v3.meta.Beta;

public class Extensions implements JdbiConfig<Extensions> {
   private final List<ExtensionFactoryDelegate> extensionFactories;
   private volatile Map<Class<?>, ExtensionMetadata> extensionMetadataCache;
   private final List<ExtensionHandlerCustomizer> extensionHandlerCustomizers;
   private final List<ExtensionHandlerFactory> extensionHandlerFactories;
   private final List<ConfigCustomizerFactory> configCustomizerFactories;
   private final AtomicBoolean allowProxy = new AtomicBoolean();
   private final AtomicBoolean failFast = new AtomicBoolean();
   private ConfigRegistry registry;

   public Extensions() {
      this.extensionFactories = new CopyOnWriteArrayList<>();
      this.extensionMetadataCache = new CopyOnWriteHashMap<>();
      this.extensionHandlerCustomizers = new CopyOnWriteArrayList<>();
      this.extensionHandlerFactories = new CopyOnWriteArrayList<>();
      this.configCustomizerFactories = new CopyOnWriteArrayList<>();
      this.allowProxy.set(true);
      this.failFast.set(false);
      this.internalRegisterHandlerFactory(DefaultMethodExtensionHandlerFactory.INSTANCE);
      this.internalRegisterHandlerFactory(BridgeMethodExtensionHandlerFactory.INSTANCE);
      this.registerHandlerFactory(UseAnnotationExtensionHandlerFactory.INSTANCE);
      this.registerHandlerCustomizer(UseAnnotationExtensionHandlerCustomizer.INSTANCE);
      this.registerConfigCustomizerFactory(UseAnnotationConfigCustomizerFactory.INSTANCE);
   }

   private Extensions(Extensions that) {
      this.extensionFactories = new CopyOnWriteArrayList<>(that.extensionFactories);
      this.extensionMetadataCache = that.extensionMetadataCache;
      this.extensionHandlerCustomizers = new CopyOnWriteArrayList<>(that.extensionHandlerCustomizers);
      this.extensionHandlerFactories = new CopyOnWriteArrayList<>(that.extensionHandlerFactories);
      this.configCustomizerFactories = new CopyOnWriteArrayList<>(that.configCustomizerFactories);
      this.allowProxy.set(that.allowProxy.get());
      this.failFast.set(that.failFast.get());
   }

   @Override
   public void setRegistry(ConfigRegistry registry) {
      this.registry = registry;
   }

   public Extensions register(ExtensionFactory factory) {
      this.invalidateMetadataCache();
      this.extensionFactories.add(0, new ExtensionFactoryDelegate(factory));
      return this;
   }

   @Alpha
   public Extensions registerHandlerFactory(ExtensionHandlerFactory extensionHandlerFactory) {
      this.invalidateMetadataCache();
      return this.internalRegisterHandlerFactory(FilteringExtensionHandlerFactory.forDelegate(extensionHandlerFactory));
   }

   @Alpha
   public Extensions registerHandlerCustomizer(ExtensionHandlerCustomizer extensionHandlerCustomizer) {
      this.invalidateMetadataCache();
      this.extensionHandlerCustomizers.add(0, extensionHandlerCustomizer);
      return this;
   }

   @Alpha
   public Extensions registerConfigCustomizerFactory(ConfigCustomizerFactory configCustomizerFactory) {
      this.invalidateMetadataCache();
      this.configCustomizerFactories.add(0, configCustomizerFactory);
      return this;
   }

   @Alpha
   public void invalidateMetadataCache() {
      this.extensionMetadataCache = new CopyOnWriteHashMap<>();
   }

   public boolean hasExtensionFor(Class<?> extensionType) {
      return this.findFactoryFor(extensionType).isPresent();
   }

   public <E> Optional<E> findFor(Class<E> extensionType, HandleSupplier handleSupplier) {
      return this.findFactoryFor(extensionType).map(factory -> factory.attach(extensionType, handleSupplier));
   }

   private Optional<ExtensionFactory> findFactoryFor(Class<?> extensionType) {
      for (ExtensionFactory factory : this.extensionFactories) {
         if (factory.accepts(extensionType)) {
            return Optional.of(factory);
         }
      }

      return Optional.empty();
   }

   public Optional<ExtensionFactory> findFactory(Class<? extends ExtensionFactory> factoryType) {
      for (ExtensionFactoryDelegate factory : this.extensionFactories) {
         if (factoryType.isInstance(factory.getDelegatedFactory())) {
            return Optional.of(factory);
         }
      }

      return Optional.empty();
   }

   @Alpha
   public ExtensionMetadata findMetadata(Class<?> extensionType, ExtensionFactory extensionFactory) {
      return this.extensionMetadataCache.computeIfAbsent(extensionType, this.createMetadata(extensionFactory));
   }

   private Extensions internalRegisterHandlerFactory(ExtensionHandlerFactory extensionHandlerFactory) {
      this.extensionHandlerFactories.add(0, extensionHandlerFactory);
      return this;
   }

   private Function<Class<?>, ExtensionMetadata> createMetadata(ExtensionFactory extensionFactory) {
      return extensionType -> {
         ExtensionMetadata.Builder builder = ExtensionMetadata.builder(extensionType);
         extensionFactory.getExtensionHandlerFactories(this.registry).forEach(builder::addExtensionHandlerFactory);
         this.extensionHandlerFactories.forEach(builder::addExtensionHandlerFactory);
         if (extensionFactory.getFactoryFlags().contains(ExtensionFactory.FactoryFlag.NON_VIRTUAL_FACTORY)) {
            builder.addExtensionHandlerFactory(InstanceExtensionHandlerFactory.INSTANCE);
         }

         extensionFactory.getExtensionHandlerCustomizers(this.registry).forEach(builder::addExtensionHandlerCustomizer);
         this.extensionHandlerCustomizers.forEach(builder::addExtensionHandlerCustomizer);
         extensionFactory.getConfigCustomizerFactories(this.registry).forEach(builder::addConfigCustomizerFactory);
         this.configCustomizerFactories.forEach(builder::addConfigCustomizerFactory);
         extensionFactory.buildExtensionMetadata(builder);
         return builder.build();
      };
   }

   @Beta
   public Extensions setAllowProxy(boolean allowProxy) {
      this.allowProxy.set(allowProxy);
      return this;
   }

   @Beta
   public boolean isAllowProxy() {
      return this.allowProxy.get();
   }

   @Alpha
   public Extensions failFast() {
      this.failFast.set(true);
      return this;
   }

   @Alpha
   public boolean isFailFast() {
      return this.failFast.get();
   }

   public Extensions createCopy() {
      return new Extensions(this);
   }

   @Beta
   public void onCreateProxy() {
      if (!this.isAllowProxy()) {
         throw new IllegalStateException(
            "Creating onDemand proxy disallowed. Ensure @GenerateSqlObject annotation is being processed by `jdbi3-generator` annotation processor."
         );
      }
   }
}
