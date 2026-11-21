package net.thenextlvl.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ComponentBundleImpl implements ComponentBundle {
   private static final Logger LOGGER = LoggerFactory.getLogger("i18n");
   private final Locale fallback;
   private final Map<String, String> placeholders;
   private final MiniMessageTranslationStore translator;

   private ComponentBundleImpl(Locale fallback, Map<String, String> placeholders, MiniMessageTranslationStore translator) {
      this.fallback = fallback;
      this.placeholders = Map.copyOf(placeholders);
      this.translator = translator;
   }

   @Override
   public MiniMessageTranslationStore translator() {
      return this.translator;
   }

   public ComponentBundleImpl registerTranslations() throws IllegalStateException {
      if (GlobalTranslator.translator().addSource(this.translator)) {
         return this;
      } else {
         throw new IllegalStateException("Translation store '" + this.translator.name() + "' already registered");
      }
   }

   @Override
   public void unregisterTranslations() throws IllegalStateException {
      if (!GlobalTranslator.translator().removeSource(this.translator)) {
         throw new IllegalStateException("Translation store '" + this.translator.name() + "' not registered");
      }
   }

   @Nullable
   @Override
   public Component translate(String translationKey, Audience audience, ComponentLike... arguments) {
      return this.translate(Component.translatable(translationKey, arguments), audience);
   }

   @Nullable
   @Override
   public Component translate(String translationKey, Audience audience) {
      return this.translate(translationKey, audience);
   }

   @Nullable
   @Override
   public Component translate(String translationKey, Locale locale, ComponentLike... arguments) {
      return this.translate(Component.translatable(translationKey, arguments), locale);
   }

   @Nullable
   @Override
   public Component translate(String translationKey, Locale locale) {
      return this.translate(translationKey, locale);
   }

   @Nullable
   @Override
   public Component translate(TranslatableComponent component, Audience audience) {
      return this.translate(component, audience.get(Identity.LOCALE).orElse(this.fallback));
   }

   @Nullable
   @Override
   public Component translate(TranslatableComponent component, Locale locale) {
      if (!this.placeholders.isEmpty()) {
         component = component.arguments(this.placeholders(locale, component.arguments()));
      }

      return this.translator.translate(component, locale);
   }

   private List<ComponentLike> placeholders(Locale locale, List<TranslationArgument> arguments) {
      ArrayList<ComponentLike> result = new ArrayList<>(arguments.size() + this.placeholders.size());

      for (Entry<String, String> entry : this.placeholders.entrySet()) {
         Component translated = this.translator.translate(Component.translatable(entry.getValue()), locale);
         if (translated != null) {
            result.add(Argument.component(entry.getKey(), translated));
         } else {
            result.add(Component.text(entry.getValue(), NamedTextColor.RED));
         }
      }

      result.addAll(arguments);
      return result;
   }

   @Override
   public void sendMessage(Audience audience, String translationKey) {
      this.sendMessage(audience, translationKey);
   }

   @Override
   public void sendMessage(Audience audience, String translationKey, ComponentLike... arguments) {
      Component translated = this.translate(translationKey, audience, arguments);
      if (translated != null && !Component.empty().equals(translated)) {
         audience.sendMessage(translated);
      }
   }

   @Override
   public void sendMessage(Audience audience, String translationKey, TagResolver... resolver) {
      this.sendMessage(audience, translationKey, Argument.tagResolver(resolver));
   }

   @Override
   public void sendActionBar(Audience audience, String translationKey, ComponentLike... arguments) {
      Component translated = this.translate(translationKey, audience, arguments);
      if (translated != null && !Component.empty().equals(translated)) {
         audience.sendActionBar(translated);
      }
   }

   @Override
   public void showTitle(Audience audience, @Nullable String title, @Nullable String subtitle, @Nullable Times times, ComponentLike... arguments) {
      Component titleComponent = title != null ? this.translate(title, audience, arguments) : null;
      Component subtitleComponent = subtitle != null ? this.translate(subtitle, audience, arguments) : null;
      if (subtitleComponent != null || titleComponent != null) {
         audience.showTitle(
            Title.title(
               (Component)(titleComponent != null ? titleComponent : Component.empty()),
               (Component)(subtitleComponent != null ? subtitleComponent : Component.empty()),
               times
            )
         );
      }
   }

   @Override
   public void showTitle(Audience audience, @Nullable String title, @Nullable String subtitle, ComponentLike... arguments) {
      this.showTitle(audience, title, subtitle, Title.DEFAULT_TIMES, arguments);
   }

   @Override
   public Component component(String translationKey, Audience audience) {
      return this.component(translationKey, audience);
   }

   @Override
   public Component component(String translationKey, Audience audience, ComponentLike... arguments) {
      Locale locale = audience.get(Identity.LOCALE).orElse(this.fallback);
      return this.component(translationKey, locale, arguments);
   }

   @Override
   public Component component(String translationKey, Audience audience, TagResolver... resolvers) {
      return this.component(translationKey, audience, Argument.tagResolver(resolvers));
   }

   @Override
   public Component component(String translationKey, Locale locale) {
      return this.component(translationKey, locale);
   }

   @Override
   public Component component(String translationKey, Locale locale, ComponentLike... arguments) {
      Component translated = this.translate(translationKey, locale, arguments);
      return (Component)(translated != null ? translated : Component.text(translationKey, NamedTextColor.RED));
   }

   @Override
   public Component component(String translationKey, Locale locale, TagResolver... resolvers) {
      return this.component(translationKey, locale, Argument.tagResolver(resolvers));
   }

   public static final class Builder implements ComponentBundle.Builder {
      private final Map<String, Locale> files = new HashMap<>();
      private final Map<String, String> placeholders = new HashMap<>();
      @Nullable
      private ResourceMigrator migrator = null;
      private Charset charset = StandardCharsets.UTF_8;
      private Locale fallback = Locale.US;
      private MiniMessage miniMessage = MiniMessage.miniMessage();
      private ComponentBundle.Scope scope = ComponentBundle.Scope.FILTER_AND_FILL;
      private Key name;
      private Path path;

      Builder(Key name, Path path) {
         this.name = name;
         this.path = path;
      }

      @Override
      public ComponentBundle.Builder charset(Charset charset) {
         this.charset = charset;
         return this;
      }

      @Override
      public ComponentBundle.Builder fallback(Locale fallback) {
         this.fallback = fallback;
         return this;
      }

      @Override
      public ComponentBundle.Builder migrator(@Nullable ResourceMigrator migrator) {
         this.migrator = migrator;
         return this;
      }

      @Override
      public ComponentBundle.Builder miniMessage(MiniMessage miniMessage) {
         this.miniMessage = miniMessage;
         return this;
      }

      @Override
      public ComponentBundle.Builder name(Key name) {
         this.name = name;
         return this;
      }

      @Override
      public ComponentBundle.Builder path(Path path) {
         this.path = path;
         return this;
      }

      public ComponentBundleImpl.Builder resource(String name, Locale locale) throws IllegalStateException {
         String suffix = ".properties";
         String key = name.endsWith(suffix) ? name : name + suffix;
         if (this.files.put(key, locale) == null) {
            return this;
         } else {
            throw new IllegalStateException("Resource '" + key + "' already registered for locale " + locale);
         }
      }

      @Override
      public ComponentBundle.Builder scope(ComponentBundle.Scope scope) {
         this.scope = scope;
         return this;
      }

      @Override
      public ComponentBundle.Builder placeholder(@TagPattern String name, String translationKey) {
         this.placeholders.put(name, translationKey);
         return this;
      }

      @Override
      public ComponentBundle build() throws ResourceMigrationException {
         MiniMessageTranslationStore registry = MiniMessageTranslationStore.create(this.name, this.miniMessage);
         registry.defaultLocale(this.fallback);
         this.registerResources(registry);
         return new ComponentBundleImpl(this.fallback, this.placeholders, registry);
      }

      private void registerResources(MiniMessageTranslationStore registry) {
         this.files.forEach((name, locale) -> {
            try {
               registry.registerAll(locale, this.extractResource(name, locale));
            } catch (IOException var5) {
               ComponentBundleImpl.LOGGER.error("Failed to register resource '{}' ({})", new Object[]{name, locale, var5});
            }
         });
      }

      @Unmodifiable
      private Map<String, String> extractResource(String baseName, Locale locale) throws IOException {
         PropertiesFile file = new PropertiesFile(this.path.resolve(baseName), this.charset, this.readResource(baseName));
         this.migrate(baseName, locale, file);
         file.validate(this.scope);
         if (file.getRoot().isEmpty()) {
            Files.deleteIfExists(file.getFile());
         } else {
            file.save();
         }

         HashMap<String, String> properties = new HashMap<>(file.getRoot().size());
         file.getRoot().forEach((key, value) -> properties.put(key.toString(), value.toString()));
         return properties;
      }

      private Properties readResource(String name) throws IOException {
         Properties var3;
         try (InputStream resource = this.getClass().getClassLoader().getResourceAsStream(name)) {
            if (resource == null) {
               throw new IOException("Resource '" + name + "' not found in classpath");
            }

            var3 = this.readResource(resource);
         }

         return var3;
      }

      private Properties readResource(InputStream resource) throws IOException {
         Properties var5;
         try (
            InputStreamReader reader = new InputStreamReader(resource, this.charset);
            BufferedReader buffer = new BufferedReader(reader);
         ) {
            Properties properties = new Properties();
            properties.load(buffer);
            var5 = properties;
         }

         return var5;
      }

      private void migrate(String baseName, Locale locale, PropertiesFile file) throws IOException {
         String oldResource = this.migrator != null ? this.migrator.getOldResourceName(locale) : null;
         Path oldPath = this.migrator != null ? this.migrator.getOldPath() : null;
         if (this.path.equals(oldPath)) {
            throw new ResourceMigrationException("New and old path cannot match");
         } else {
            boolean migrate = (oldPath != null || !baseName.equals(oldResource)) && (oldResource != null || oldPath != null);
            if (migrate) {
               this.migrate(baseName, file, oldPath, oldResource);
            }

            if (migrate || Files.isRegularFile(file.getFile())) {
               try {
                  this.migrateResource(baseName, locale, file);
               } catch (Exception var8) {
                  throw new ResourceMigrationException("An error occurred while migrating resource '" + file.getFile() + "'", var8);
               }
            }
         }
      }

      private void migrate(String baseName, PropertiesFile file, @Nullable Path oldPath, @Nullable String oldResource) throws IOException {
         Path actualPath = oldPath != null ? oldPath : this.path;
         String actualResource = oldResource != null ? oldResource : baseName;
         PropertiesFile oldFile = new PropertiesFile(actualPath.resolve(actualResource), this.charset, new Properties());
         file.merge(oldFile.getRoot());
         if (Files.deleteIfExists(oldFile.getFile())) {
            ComponentBundleImpl.LOGGER.debug("Migrated resource '{}' to '{}'", oldFile.getFile(), file.getFile());
         } else {
            ComponentBundleImpl.LOGGER.warn("Failed to delete old resource '{}'", oldFile.getFile());
         }
      }

      private void migrateResource(String resource, Locale locale, PropertiesFile file) {
         if (this.migrator != null && this.migrator.shouldMigrate(resource, file.getRoot())) {
            Properties migrated = new Properties(file.getRoot().size());
            file.getRoot().forEach((key, message) -> {
               ResourceMigrator.Migration migration = this.migrator.migrate(locale, key.toString(), message.toString());
               if (migration == null) {
                  migrated.put(key, message);
               } else if (!migration.drop()) {
                  Object migratedKey = migration.key() != null ? migration.key() : key;
                  Object migratedMessage = migration.message() != null ? migration.message() : message;
                  migrated.put(migratedKey, migratedMessage);
               }
            });
            file.setRoot(migrated);
         }
      }
   }
}
