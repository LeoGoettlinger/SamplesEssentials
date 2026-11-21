package org.jdbi.v3.core.extension;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.internal.JdbiClassUtils;

final class ExtensionFactoryDelegate implements ExtensionFactory {
   private final ExtensionFactory delegatedFactory;

   ExtensionFactoryDelegate(ExtensionFactory delegatedFactory) {
      this.delegatedFactory = delegatedFactory;
   }

   @Override
   public boolean accepts(Class<?> extensionType) {
      return this.delegatedFactory.accepts(extensionType);
   }

   ExtensionFactory getDelegatedFactory() {
      return this.delegatedFactory;
   }

   @Override
   public Collection<ExtensionHandlerFactory> getExtensionHandlerFactories(ConfigRegistry config) {
      return Collections.unmodifiableCollection(
         this.delegatedFactory.getExtensionHandlerFactories(config).stream().map(FilteringExtensionHandlerFactory::forDelegate).toList()
      );
   }

   @Override
   public Collection<ExtensionHandlerCustomizer> getExtensionHandlerCustomizers(ConfigRegistry config) {
      return this.delegatedFactory.getExtensionHandlerCustomizers(config);
   }

   @Override
   public Collection<ConfigCustomizerFactory> getConfigCustomizerFactories(ConfigRegistry config) {
      return this.delegatedFactory.getConfigCustomizerFactories(config);
   }

   @Override
   public void buildExtensionMetadata(ExtensionMetadata.Builder builder) {
      this.delegatedFactory.buildExtensionMetadata(builder);
   }

   @Override
   public Set<ExtensionFactory.FactoryFlag> getFactoryFlags() {
      return this.delegatedFactory.getFactoryFlags();
   }

   @Override
   public <E> E attach(Class<E> extensionType, HandleSupplier handleSupplier) {
      Set<ExtensionFactory.FactoryFlag> factoryFlags = this.getFactoryFlags();
      if (factoryFlags.contains(ExtensionFactory.FactoryFlag.DONT_USE_PROXY)) {
         return this.delegatedFactory.attach(extensionType, handleSupplier);
      } else if (extensionType != null && extensionType.isInterface()) {
         ConfigRegistry config = handleSupplier.getConfig();
         Extensions extensions = config.get(Extensions.class);
         extensions.onCreateProxy();
         ExtensionMetadata extensionMetaData = extensions.findMetadata(extensionType, this.delegatedFactory);
         ConfigRegistry instanceConfig = extensionMetaData.createInstanceConfiguration(config);
         Map<JdbiClassUtils.MethodKey, ExtensionMetadata.ExtensionHandlerInvoker> handlers = new HashMap<>();
         Object proxy = Proxy.newProxyInstance(
            extensionType.getClassLoader(),
            new Class[]{extensionType},
            (proxyInstance, method, args) -> handlers.get(JdbiClassUtils.MethodKey.methodKey(method)).invoke(args)
         );
         Object delegatedInstance = factoryFlags.contains(ExtensionFactory.FactoryFlag.NON_VIRTUAL_FACTORY)
            ? this.delegatedFactory.attach(extensionType, handleSupplier)
            : proxy;
         ExtensionHandler toStringHandler = (methodConfig, target) -> (h, args) -> "Jdbi extension proxy for "
            + extensionType.getName()
            + "@"
            + Integer.toHexString(proxy.hashCode());
         handlers.put(
            JdbiClassUtils.MethodKey.methodKey(JdbiClassUtils.TOSTRING_METHOD),
            extensionMetaData.new ExtensionHandlerInvoker(proxy, JdbiClassUtils.TOSTRING_METHOD, toStringHandler, handleSupplier, instanceConfig)
         );
         handlers.put(
            JdbiClassUtils.MethodKey.methodKey(JdbiClassUtils.EQUALS_METHOD),
            extensionMetaData.new ExtensionHandlerInvoker(
               proxy, JdbiClassUtils.EQUALS_METHOD, BuiltInExtensionHandler.EQUALS_HANDLER, handleSupplier, instanceConfig
            )
         );
         handlers.put(
            JdbiClassUtils.MethodKey.methodKey(JdbiClassUtils.HASHCODE_METHOD),
            extensionMetaData.new ExtensionHandlerInvoker(
               proxy, JdbiClassUtils.HASHCODE_METHOD, BuiltInExtensionHandler.HASHCODE_HANDLER, handleSupplier, instanceConfig
            )
         );
         extensionMetaData.getExtensionMethods()
            .forEach(
               method -> handlers.put(
                  JdbiClassUtils.MethodKey.methodKey(method),
                  extensionMetaData.createExtensionHandlerInvoker(delegatedInstance, method, handleSupplier, instanceConfig)
               )
            );
         extensionMetaData.getFinalizer()
            .ifPresent(
               method -> handlers.put(
                  JdbiClassUtils.MethodKey.methodKey(method),
                  extensionMetaData.new ExtensionHandlerInvoker(proxy, method, BuiltInExtensionHandler.NULL_HANDLER, handleSupplier, instanceConfig)
               )
            );
         return extensionType.cast(proxy);
      } else {
         throw new IllegalArgumentException(
            String.format("Can not attach %s as an extension with %s", extensionType, this.delegatedFactory.getClass().getSimpleName())
         );
      }
   }

   @Override
   public String toString() {
      return "ExtensionFactoryDelegate for " + this.delegatedFactory.toString();
   }
}
