package com.earth2me.essentials.libs.configurate.loader;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

final class ConfigurationFormats {
   private static final boolean USE_OWN_CLASSLOADER;
   private static final String[] OWN_CLASSLOADER_SENTINEL_CLASSES = new String[]{"org.bukkit.Bukkit"};
   static final Map<String, ConfigurationFormats.Holder> BY_EXTENSION;
   static final Set<ConfigurationFormats.Holder> FORMATS;

   private ConfigurationFormats() {
   }

   static Set<ConfigurationFormat> unwrappedFormats() {
      return ConfigurationFormats.UnwrappedFormats.UNWRAPPED_FORMATS;
   }

   static {
      String property = System.getProperty("configurate.services.discoverOnOwnClassloader");
      boolean useOwnLoader;
      if (property != null) {
         useOwnLoader = Boolean.parseBoolean(property);
      } else {
         useOwnLoader = false;

         for (String clazz : OWN_CLASSLOADER_SENTINEL_CLASSES) {
            try {
               Class.forName(clazz);
               useOwnLoader = true;
               break;
            } catch (ClassNotFoundException var10) {
            }
         }
      }

      USE_OWN_CLASSLOADER = useOwnLoader;
      Map<String, ConfigurationFormats.Holder> byExtension = new HashMap<>();
      Set<ConfigurationFormats.Holder> formats = new HashSet<>();
      ServiceLoader<ConfigurationFormat> loader;
      if (USE_OWN_CLASSLOADER) {
         loader = ServiceLoader.load(ConfigurationFormat.class, ConfigurationFormats.class.getClassLoader());
      } else {
         loader = ServiceLoader.load(ConfigurationFormat.class);
      }

      Iterator<ConfigurationFormat> it = loader.iterator();

      while (it.hasNext()) {
         ConfigurationFormats.Holder holder;
         try {
            holder = new ConfigurationFormats.Holder(it.next());
         } catch (ServiceConfigurationError var9) {
            holder = new ConfigurationFormats.Holder(var9);
         }

         formats.add(holder);
         if (holder.successful()) {
            ConfigurationFormat format = holder.format;
            if (format != null) {
               for (String ext : Objects.requireNonNull(format.supportedExtensions(), () -> "The supported extensions field was improperly null on " + format)) {
                  byExtension.put(ext, holder);
               }
            }
         }
      }

      BY_EXTENSION = UnmodifiableCollections.copyOf(byExtension);
      FORMATS = UnmodifiableCollections.copyOf(formats);
   }

   static final class Holder {
      @Nullable
      private final ConfigurationFormat format;
      @Nullable
      private final ServiceConfigurationError error;

      Holder(final ConfigurationFormat format) {
         this.format = format;
         this.error = null;
      }

      Holder(final ServiceConfigurationError error) {
         this.format = null;
         this.error = error;
      }

      ConfigurationFormat get() {
         if (this.error != null) {
            throw this.error;
         } else {
            assert this.format != null;

            return this.format;
         }
      }

      boolean successful() {
         return this.format != null;
      }
   }

   static final class UnwrappedFormats {
      static final Set<ConfigurationFormat> UNWRAPPED_FORMATS;

      private UnwrappedFormats() {
      }

      static {
         Set<ConfigurationFormat> formats = new HashSet<>(ConfigurationFormats.FORMATS.size());

         for (ConfigurationFormats.Holder format : ConfigurationFormats.FORMATS) {
            formats.add(format.get());
         }

         UNWRAPPED_FORMATS = UnmodifiableCollections.copyOf(formats);
      }
   }
}
