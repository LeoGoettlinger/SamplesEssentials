package net.kyori.adventure.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import net.kyori.adventure.internal.properties.AdventureProperties;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public final class Services {
   private static final boolean SERVICE_LOAD_FAILURES_ARE_FATAL = Boolean.TRUE.equals(AdventureProperties.SERVICE_LOAD_FAILURES_ARE_FATAL.value());

   private Services() {
   }

   @Deprecated(
      since = "5.1.0",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "6.0.0"
   )
   public static <P> Optional<P> service(final Class<P> type) {
      return service(ServiceLoader.load(type, type.getClassLoader()), type);
   }

   public static <P> Optional<P> service(final ServiceLoader<P> loader, final Class<P> type) {
      Iterator<P> it = loader.iterator();

      while (it.hasNext()) {
         P instance;
         try {
            instance = it.next();
         } catch (Throwable var5) {
            if (!SERVICE_LOAD_FAILURES_ARE_FATAL) {
               continue;
            }

            throw new IllegalStateException("Encountered an exception loading service " + type, var5);
         }

         if (it.hasNext()) {
            throw new IllegalStateException("Expected to find one service " + type + ", found multiple");
         }

         return Optional.of(instance);
      }

      return Optional.empty();
   }

   @Deprecated(
      since = "5.1.0",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "6.0.0"
   )
   public static <P> Optional<P> serviceWithFallback(final Class<P> type) {
      return serviceWithFallback(ServiceLoader.load(type, type.getClassLoader()), type);
   }

   public static <P> Optional<P> serviceWithFallback(final ServiceLoader<P> loader, final Class<P> type) {
      Iterator<P> it = loader.iterator();
      P firstFallback = null;

      while (it.hasNext()) {
         P instance;
         try {
            instance = it.next();
         } catch (Throwable var6) {
            if (!SERVICE_LOAD_FAILURES_ARE_FATAL) {
               continue;
            }

            throw new IllegalStateException("Encountered an exception loading service " + type, var6);
         }

         if (!(instance instanceof Services.Fallback)) {
            return Optional.of(instance);
         }

         if (firstFallback == null) {
            firstFallback = instance;
         }
      }

      return Optional.ofNullable(firstFallback);
   }

   @Deprecated(
      since = "5.1.0",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "6.0.0"
   )
   public static <P> Set<P> services(final Class<? extends P> clazz) {
      return services(ServiceLoader.load(clazz, clazz.getClassLoader()), clazz);
   }

   public static <P> Set<P> services(final ServiceLoader<? extends P> loader, final Class<? extends P> type) {
      Set<P> providers = new HashSet<>();

      for (P instance : loader) {
         try {
            ;
         } catch (ServiceConfigurationError var6) {
            if (!SERVICE_LOAD_FAILURES_ARE_FATAL) {
               continue;
            }

            throw new IllegalStateException("Encountered an exception loading a provider for " + type + ": ", var6);
         }

         providers.add(instance);
      }

      return Set.copyOf(providers);
   }

   public interface Fallback {
   }
}
