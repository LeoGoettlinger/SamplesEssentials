package com.earth2me.essentials;

import com.earth2me.essentials.paperlib.PaperLib;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import net.ess3.provider.Provider;
import net.essentialsx.providers.NullableProvider;
import net.essentialsx.providers.ProviderData;
import net.essentialsx.providers.ProviderTest;
import org.bukkit.plugin.Plugin;

public class ProviderFactory {
   private final Map<Class<? extends Provider>, Provider> providers = new HashMap<>();
   private final Map<Class<? extends Provider>, List<Class<? extends Provider>>> registeredProviders = new HashMap<>();
   private final Essentials essentials;

   public ProviderFactory(final Essentials essentials) {
      this.essentials = essentials;
   }

   public <P extends Provider> P get(final Class<P> providerClass) {
      Provider provider = this.providers.get(providerClass);
      return (P)(provider == null ? null : provider);
   }

   @SafeVarargs
   public final void registerProvider(final Class<? extends Provider>... toRegister) {
      for (Class<? extends Provider> provider : toRegister) {
         Class<?> superclass = provider.getInterfaces().length > 0 ? provider.getInterfaces()[0] : provider.getSuperclass();
         if (Provider.class.isAssignableFrom(superclass)) {
            this.registeredProviders.computeIfAbsent((Class<? extends Provider>)superclass, k -> new ArrayList<>()).add(provider);
            if (this.essentials.getSettings().isDebug()) {
               this.essentials.getLogger().info("Registered provider " + provider.getSimpleName() + " for " + superclass.getSimpleName());
            }
         }
      }
   }

   public void finalizeRegistration() {
      for (Entry<Class<? extends Provider>, List<Class<? extends Provider>>> entry : this.registeredProviders.entrySet()) {
         Class<? extends Provider> providerClass = entry.getKey();
         boolean nullable = providerClass.isAnnotationPresent(NullableProvider.class);
         Class<? extends Provider> highestProvider = null;
         ProviderData highestProviderData = null;
         int highestWeight = -1;

         for (Class<? extends Provider> provider : entry.getValue()) {
            try {
               ProviderData providerData = provider.getAnnotation(ProviderData.class);
               if (providerData.weight() > highestWeight && this.testProvider(provider)) {
                  highestWeight = providerData.weight();
                  highestProvider = provider;
                  highestProviderData = providerData;
               }
            } catch (Throwable var11) {
               this.essentials.getLogger().log(Level.SEVERE, "Failed to initialize provider " + provider.getName(), var11);
            }
         }

         if (highestProvider != null) {
            this.essentials.getLogger().info("Selected " + highestProviderData.description() + " as the provider for " + providerClass.getSimpleName());
            this.providers.put(providerClass, this.getProviderInstance(highestProvider));
         } else {
            if (!nullable) {
               throw new IllegalStateException("No provider found for " + providerClass.getName());
            }

            this.essentials.getLogger().info("No provider found for " + providerClass.getSimpleName() + ", but it is nullable");
         }
      }

      this.registeredProviders.clear();
   }

   private boolean testProvider(final Class<?> providerClass) throws InvocationTargetException, IllegalAccessException {
      try {
         for (Method method : providerClass.getMethods()) {
            if (method.isAnnotationPresent(ProviderTest.class)) {
               return (Boolean)method.invoke(null);
            }
         }

         return true;
      } catch (NoClassDefFoundError var6) {
         return false;
      }
   }

   private <P extends Provider> P getProviderInstance(final Class<P> provider) {
      try {
         Constructor<?> constructor = provider.getConstructors()[0];
         if (constructor.getParameterTypes().length == 0) {
            return (P)constructor.newInstance();
         } else {
            Object[] args = new Object[constructor.getParameterTypes().length];

            for (int i = 0; i < args.length; i++) {
               Class<?> paramType = constructor.getParameterTypes()[i];
               if (paramType.isAssignableFrom(Plugin.class)) {
                  args[i] = this.essentials;
               } else {
                  if (!paramType.isAssignableFrom(boolean.class)) {
                     throw new IllegalArgumentException("Unsupported parameter type " + paramType.getName());
                  }

                  args[i] = PaperLib.isPaper();
               }
            }

            return (P)constructor.newInstance(args);
         }
      } catch (IllegalAccessException | InvocationTargetException | InstantiationException var7) {
         try {
            return provider.getConstructor().newInstance();
         } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException var6) {
            var7.printStackTrace();
            throw new RuntimeException(var6);
         }
      }
   }
}
