package org.jdbi.v3.core.extension;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.jdbi.v3.core.config.ConfigCustomizer;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.internal.ConfigCustomizerChain;
import org.jdbi.v3.core.internal.JdbiClassUtils;
import org.jdbi.v3.core.internal.exceptions.Sneaky;
import org.jdbi.v3.meta.Alpha;

@Alpha
public final class ExtensionMetadata {
   private final Class<?> extensionType;
   private final ConfigCustomizer instanceConfigCustomizer;
   private final Map<Method, ? extends ConfigCustomizer> methodConfigCustomizers;
   private final Map<Method, ExtensionHandler> methodHandlers;
   private final Optional<Method> finalizer;

   public static ExtensionMetadata.Builder builder(Class<?> extensionType) {
      return new ExtensionMetadata.Builder(extensionType);
   }

   private ExtensionMetadata(
      Class<?> extensionType,
      ConfigCustomizer instanceConfigCustomizer,
      Map<Method, ? extends ConfigCustomizer> methodConfigCustomizers,
      Map<Method, ExtensionHandler> methodHandlers,
      Optional<Method> finalizer
   ) {
      this.extensionType = extensionType;
      this.instanceConfigCustomizer = instanceConfigCustomizer;
      this.methodConfigCustomizers = Collections.unmodifiableMap(methodConfigCustomizers);
      this.methodHandlers = Collections.unmodifiableMap(methodHandlers);
      this.finalizer = finalizer;
   }

   public Class<?> extensionType() {
      return this.extensionType;
   }

   public ConfigRegistry createInstanceConfiguration(ConfigRegistry config) {
      ConfigRegistry instanceConfiguration = config.createCopy();
      this.instanceConfigCustomizer.customize(instanceConfiguration);
      return instanceConfiguration;
   }

   public ConfigRegistry createMethodConfiguration(Method method, ConfigRegistry config) {
      ConfigRegistry methodConfiguration = config.createCopy();
      ConfigCustomizer methodConfigCustomizer = this.methodConfigCustomizers.get(method);
      if (methodConfigCustomizer != null) {
         methodConfigCustomizer.customize(methodConfiguration);
      }

      return methodConfiguration;
   }

   public Set<Method> getExtensionMethods() {
      return this.methodHandlers.keySet();
   }

   Optional<Method> getFinalizer() {
      return this.finalizer;
   }

   public <E> ExtensionMetadata.ExtensionHandlerInvoker createExtensionHandlerInvoker(
      E target, Method method, HandleSupplier handleSupplier, ConfigRegistry config
   ) {
      return new ExtensionMetadata.ExtensionHandlerInvoker(target, method, this.methodHandlers.get(method), handleSupplier, config);
   }

   public static final class Builder {
      private final Class<?> extensionType;
      private final Collection<ExtensionHandlerFactory> extensionHandlerFactories = new ArrayList<>();
      private final Collection<ExtensionHandlerCustomizer> extensionHandlerCustomizers = new ArrayList<>();
      private final Collection<ConfigCustomizerFactory> configCustomizerFactories = new ArrayList<>();
      private final ConfigCustomizerChain instanceConfigCustomizer = new ConfigCustomizerChain();
      private final Map<Method, ConfigCustomizerChain> methodConfigCustomizers = new HashMap<>();
      private final Map<Method, ExtensionHandler> methodHandlers = new HashMap<>();
      private final Optional<Method> finalizer;
      private Collection<Method> extensionTypeMethods;

      Builder(Class<?> extensionType) {
         this.extensionType = extensionType;
         this.finalizer = JdbiClassUtils.safeMethodLookup(extensionType, "finalize");
      }

      @Alpha
      public ExtensionMetadata.Builder setExtensionTypeMethods(Collection<Method> methods) {
         this.extensionTypeMethods = new HashSet<>(methods);
         return this;
      }

      private Collection<Method> discoverExtensionTypeMethods() {
         Collection<Method> methods = new HashSet<>();
         methods.addAll(Arrays.asList(this.extensionType.getMethods()));
         methods.addAll(Arrays.asList(this.extensionType.getDeclaredMethods()));
         return methods;
      }

      public ExtensionMetadata.Builder addExtensionHandlerFactory(ExtensionHandlerFactory extensionHandlerFactory) {
         this.extensionHandlerFactories.add(extensionHandlerFactory);
         return this;
      }

      public ExtensionMetadata.Builder addExtensionHandlerCustomizer(ExtensionHandlerCustomizer extensionHandlerCustomizer) {
         this.extensionHandlerCustomizers.add(extensionHandlerCustomizer);
         return this;
      }

      public ExtensionMetadata.Builder addConfigCustomizerFactory(ConfigCustomizerFactory configCustomizerFactory) {
         this.configCustomizerFactories.add(configCustomizerFactory);
         return this;
      }

      public ExtensionMetadata.Builder addInstanceConfigCustomizer(ConfigCustomizer configCustomizer) {
         this.instanceConfigCustomizer.addCustomizer(configCustomizer);
         return this;
      }

      public ExtensionMetadata.Builder addMethodConfigCustomizer(Method method, ConfigCustomizer configCustomizer) {
         ConfigCustomizerChain methodConfigCustomizer = this.methodConfigCustomizers.computeIfAbsent(method, m -> new ConfigCustomizerChain());
         methodConfigCustomizer.addCustomizer(configCustomizer);
         return this;
      }

      public ExtensionMetadata.Builder addMethodHandler(Method method, ExtensionHandler handler) {
         this.methodHandlers.put(method, handler);
         return this;
      }

      public Class<?> getExtensionType() {
         return this.extensionType;
      }

      public ExtensionMetadata build() {
         if (this.extensionTypeMethods == null) {
            this.extensionTypeMethods = this.discoverExtensionTypeMethods();
         }

         this.extensionTypeMethods
            .stream()
            .filter(m -> !m.isSynthetic())
            .collect(Collectors.groupingBy(JdbiClassUtils.MethodKey::methodKey))
            .values()
            .stream()
            .filter(methodCount -> methodCount.size() > 1)
            .findAny()
            .ifPresent(
               methods -> {
                  throw new UnableToCreateExtensionException(
                     "%s has ambiguous methods (%s) found, please resolve with an explicit override", this.extensionType, methods
                  );
               }
            );
         Set<Method> seen = new HashSet<>(this.methodHandlers.keySet());

         for (Method method : this.extensionTypeMethods) {
            if (!Modifier.isStatic(method.getModifiers()) && seen.add(method)) {
               ExtensionHandler handler = this.findExtensionHandlerFor(this.extensionType, method)
                  .orElseGet(() -> ExtensionHandler.missingExtensionHandler(method));

               for (ExtensionHandlerCustomizer extensionHandlerCustomizer : this.extensionHandlerCustomizers) {
                  handler = extensionHandlerCustomizer.customize(handler, this.extensionType, method);
               }

               this.methodHandlers.put(method, handler);
            }
         }

         this.configCustomizerFactories
            .forEach(configCustomizerFactory -> configCustomizerFactory.forExtensionType(this.extensionType).forEach(this::addInstanceConfigCustomizer));

         for (Method methodx : this.methodHandlers.keySet()) {
            this.configCustomizerFactories
               .forEach(
                  configCustomizerFactory -> configCustomizerFactory.forExtensionMethod(this.extensionType, method)
                     .forEach(configCustomizer -> this.addMethodConfigCustomizer(method, configCustomizer))
               );
         }

         return new ExtensionMetadata(this.extensionType, this.instanceConfigCustomizer, this.methodConfigCustomizers, this.methodHandlers, this.finalizer);
      }

      private Optional<ExtensionHandler> findExtensionHandlerFor(Class<?> extensionType, Method method) {
         for (ExtensionHandlerFactory extensionHandlerFactory : this.extensionHandlerFactories) {
            if (extensionHandlerFactory.accepts(extensionType, method)) {
               Optional<ExtensionHandler> result = extensionHandlerFactory.createExtensionHandler(extensionType, method);
               if (result.isPresent()) {
                  return result;
               }
            }
         }

         return Optional.empty();
      }
   }

   public final class ExtensionHandlerInvoker {
      private final HandleSupplier handleSupplier;
      private final ExtensionContext extensionContext;
      private final AttachedExtensionHandler extensionInvoker;

      ExtensionHandlerInvoker(Object target, Method method, ExtensionHandler extensionHandler, HandleSupplier handleSupplier, ConfigRegistry config) {
         this.handleSupplier = handleSupplier;
         ConfigRegistry methodConfig = ExtensionMetadata.this.createMethodConfiguration(method, config);
         this.extensionContext = ExtensionContext.forExtensionMethod(methodConfig, ExtensionMetadata.this.extensionType, method);
         this.extensionInvoker = extensionHandler.attachTo(methodConfig, target);

         try {
            this.extensionInvoker.warm(methodConfig);
         } catch (Exception var9) {
            if (config.get(Extensions.class).isFailFast()) {
               throw new UnableToCreateExtensionException(var9, "While inspecting %s: %s", method, var9.getMessage());
            }
         }
      }

      public Object invoke(Object... args) {
         Object[] handlerArgs = JdbiClassUtils.safeVarargs(args);
         Callable<Object> callable = () -> this.extensionInvoker.invoke(this.handleSupplier, handlerArgs);
         return this.call(callable);
      }

      public Object call(Callable<?> callable) {
         try {
            return this.handleSupplier.invokeInContext(this.extensionContext, callable);
         } catch (Exception var3) {
            throw Sneaky.throwAnyway(var3);
         }
      }

      public void call(Runnable runnable) {
         this.call(() -> {
            runnable.run();
            return null;
         });
      }
   }
}
