package org.jdbi.v3.core.internal;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.extension.Extensions;
import org.jdbi.v3.core.internal.exceptions.Sneaky;

public class OnDemandExtensions implements JdbiConfig<OnDemandExtensions> {
   private OnDemandExtensions.Factory onDemandExtensionFactory;

   public OnDemandExtensions() {
      this.onDemandExtensionFactory = (jdbi, extensionType, extraTypes) -> Optional.empty();
   }

   private OnDemandExtensions(OnDemandExtensions other) {
      this.onDemandExtensionFactory = other.onDemandExtensionFactory;
   }

   public OnDemandExtensions setFactory(OnDemandExtensions.Factory onDemandExtensionFactory) {
      this.onDemandExtensionFactory = onDemandExtensionFactory;
      return this;
   }

   public <E> E create(Jdbi jdbi, Class<E> extensionType, Class<?>... extraTypes) {
      return extensionType.cast(
         this.onDemandExtensionFactory.onDemand(jdbi, extensionType, extraTypes).orElseGet(() -> this.createProxy(jdbi, extensionType, extraTypes))
      );
   }

   private Object createProxy(Jdbi jdbi, Class<?> extensionType, Class<?>... extraTypes) {
      jdbi.getConfig(Extensions.class).onCreateProxy();
      InvocationHandler handler = (proxy, method, args) -> {
         if (JdbiClassUtils.EQUALS_METHOD.equals(method)) {
            return proxy == args[0];
         } else if (JdbiClassUtils.HASHCODE_METHOD.equals(method)) {
            return System.identityHashCode(proxy);
         } else {
            return JdbiClassUtils.TOSTRING_METHOD.equals(method)
               ? "Jdbi on demand proxy for " + extensionType.getName() + "@" + Integer.toHexString(System.identityHashCode(proxy))
               : jdbi.withExtension(extensionType, extension -> invoke(extension, method, args));
         }
      };
      LinkedHashSet<Class<?>> types = new LinkedHashSet<>();
      types.add(extensionType);
      types.addAll(Arrays.asList(extensionType.getInterfaces()));
      types.addAll(Arrays.asList(extraTypes));
      return Proxy.newProxyInstance(extensionType.getClassLoader(), types.toArray(new Class[0]), handler);
   }

   private static Object invoke(Object target, Method method, Object[] args) {
      try {
         return Proxy.isProxyClass(target.getClass())
            ? Proxy.getInvocationHandler(target).invoke(target, method, args)
            : MethodHandles.lookup().unreflect(method).bindTo(target).invokeWithArguments(args);
      } catch (Throwable var4) {
         throw Sneaky.throwAnyway(var4);
      }
   }

   public OnDemandExtensions createCopy() {
      return new OnDemandExtensions(this);
   }

   @FunctionalInterface
   public interface Factory {
      Optional<Object> onDemand(Jdbi jdbi, Class<?> extensionType, Class<?>... extraTypes);
   }
}
