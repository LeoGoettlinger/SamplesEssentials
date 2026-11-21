package net.kyori.adventure.internal.properties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Function;
import net.kyori.adventure.util.Services;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

final class AdventurePropertiesImpl {
   private static final String FILESYSTEM_DIRECTORY_NAME = "config";
   private static final String FILESYSTEM_FILE_NAME = "adventure.properties";
   private static final Properties PROPERTIES = new Properties();

   private static void print(final Throwable ex) {
      ex.printStackTrace();
   }

   private AdventurePropertiesImpl() {
   }

   @VisibleForTesting
   static String systemPropertyName(final String name) {
      return String.join(".", "net", "kyori", "adventure", name);
   }

   static <T> AdventureProperties.Property<T> property(
      final String name, final Function<String, T> parser, @Nullable final T defaultValue, final boolean allowProviderDefaultOverride
   ) {
      return new AdventurePropertiesImpl.PropertyImpl<>(name, parser, defaultValue, allowProviderDefaultOverride);
   }

   static {
      Path path = Optional.ofNullable(System.getProperty(systemPropertyName("config")))
         .map(x$0 -> Paths.get(x$0))
         .orElseGet(() -> Paths.get("config", "adventure.properties"));
      if (Files.isRegularFile(path)) {
         try (InputStream is = Files.newInputStream(path)) {
            PROPERTIES.load(is);
         } catch (IOException var6) {
            print(var6);
         }
      }
   }

   private static final class PropertyImpl<T> implements AdventureProperties.Property<T> {
      private final String name;
      private final Function<String, T> parser;
      @Nullable
      private final T defaultValue;
      private final boolean allowProviderDefaultOverride;
      private boolean valueCalculated;
      @Nullable
      private T value;

      PropertyImpl(final String name, final Function<String, T> parser, @Nullable final T defaultValue, final boolean allowProviderDefaultOverride) {
         this.name = name;
         this.parser = parser;
         this.defaultValue = defaultValue;
         this.allowProviderDefaultOverride = allowProviderDefaultOverride;
      }

      @Nullable
      @Override
      public T value() {
         if (!this.valueCalculated) {
            String property = AdventurePropertiesImpl.systemPropertyName(this.name);
            String value = System.getProperty(property, AdventurePropertiesImpl.PROPERTIES.getProperty(this.name));
            if (value != null) {
               this.value = this.parser.apply(value);
            }

            if (this.value == null) {
               if (this.allowProviderDefaultOverride) {
                  this.value = AdventurePropertiesImpl.Providers.DEFAULT_PROVIDER
                     .<T>map(provider -> provider.overrideDefault(this, this.defaultValue))
                     .orElse(this.defaultValue);
               } else {
                  this.value = this.defaultValue;
               }
            }

            this.valueCalculated = true;
         }

         return this.value;
      }

      @Override
      public boolean equals(@Nullable final Object that) {
         return this == that;
      }

      @Override
      public int hashCode() {
         return this.name.hashCode();
      }
   }

   static final class Providers {
      static final Optional<AdventureProperties.DefaultOverrideProvider> DEFAULT_PROVIDER = Services.service(
         ServiceLoader.load(AdventureProperties.DefaultOverrideProvider.class, AdventureProperties.DefaultOverrideProvider.class.getClassLoader()),
         AdventureProperties.DefaultOverrideProvider.class
      );
   }
}
