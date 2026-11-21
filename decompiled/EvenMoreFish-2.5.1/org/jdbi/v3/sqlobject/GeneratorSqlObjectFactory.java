package org.jdbi.v3.sqlobject;

import java.util.EnumSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.ExtensionFactory;
import org.jdbi.v3.core.extension.ExtensionMetadata;
import org.jdbi.v3.core.extension.Extensions;
import org.jdbi.v3.core.extension.HandleSupplier;
import org.jdbi.v3.core.internal.OnDemandExtensions;

final class GeneratorSqlObjectFactory extends AbstractSqlObjectFactory implements OnDemandExtensions.Factory {
   private final ConcurrentMap<Class<?>, GeneratedSqlObjectProvider> providerCache = new ConcurrentHashMap<>();

   @Override
   public boolean accepts(Class<?> extensionType) {
      return isConcrete(extensionType);
   }

   @Override
   public Set<ExtensionFactory.FactoryFlag> getFactoryFlags() {
      return EnumSet.of(ExtensionFactory.FactoryFlag.DONT_USE_PROXY);
   }

   @Override
   public <E> E attach(Class<E> extensionType, HandleSupplier handleSupplier) {
      if (!isConcrete(extensionType)) {
         throw new IllegalStateException(String.format("Can not process %s, not generated SQL object", extensionType.getSimpleName()));
      } else {
         ConfigRegistry config = handleSupplier.getConfig();
         ExtensionMetadata extensionMetaData = config.get(Extensions.class).findMetadata(extensionType, this);
         ConfigRegistry instanceConfig = extensionMetaData.createInstanceConfiguration(config);
         return (E)this.findProvider(extensionType).createInstance(extensionMetaData, handleSupplier, instanceConfig);
      }
   }

   @Override
   public Optional<Object> onDemand(Jdbi jdbi, Class<?> extensionType, Class<?>... extraTypes) {
      return !isConcrete(extensionType) ? Optional.empty() : Optional.of(this.findProvider(extensionType).createOnDemand(jdbi));
   }

   @Override
   public void buildExtensionMetadata(ExtensionMetadata.Builder builder) {
      super.buildExtensionMetadata(builder);
      builder.setExtensionTypeMethods(this.findProvider(builder.getExtensionType()).extensionMethods());
   }

   private GeneratedSqlObjectProvider findProvider(Class<?> extensionType) {
      return this.providerCache.computeIfAbsent(extensionType, GeneratorSqlObjectFactory::loadProvider);
   }

   private static GeneratedSqlObjectProvider loadProvider(Class<?> extensionType) {
      for (GeneratedSqlObjectProvider provider : ServiceLoader.load(GeneratedSqlObjectProvider.class, extensionType.getClassLoader())) {
         if (provider.extensionType() == extensionType) {
            return provider;
         }
      }

      throw new UnableToCreateSqlObjectException(
         String.format(
            "No %s registered for %s. Compile the type with the jdbi3-generator annotation processor of this Jdbi version.",
            GeneratedSqlObjectProvider.class.getSimpleName(),
            extensionType.getName()
         )
      );
   }
}
