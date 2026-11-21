package su.nightexpress.nightcore.config;

import java.util.Locale;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightDataPlugin;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.database.DatabaseConfig;
import su.nightexpress.nightcore.util.text.tag.Tags;

public class PluginDetails implements Writeable {
   private final String name;
   private final String prefix;
   private final String[] commandAliases;
   private final String language;
   @Deprecated
   private DatabaseConfig databaseConfig;
   private Class<?> configClass;
   @Deprecated
   private Class<?> langClass;
   private Class<?> permissionsClass;

   public PluginDetails(@NonNull String name, @NonNull String prefix, String[] commandAliases, @NonNull String language) {
      this.name = name;
      this.prefix = prefix;
      this.commandAliases = commandAliases;
      this.language = language.toLowerCase();
   }

   @NonNull
   public static PluginDetails create(@NonNull String name, String[] commandAliases) {
      String prefix = Tags.LIGHT_YELLOW.enclose(Tags.BOLD.enclose(name)) + Tags.DARK_GRAY.enclose(" » ") + Tags.GRAY.getBracketsName();
      String language = Locale.getDefault().getLanguage();
      return new PluginDetails(name, prefix, commandAliases, language);
   }

   @NonNull
   public static PluginDetails read(@NonNull NightPlugin plugin, @NonNull FileConfig config, @NonNull PluginDetails defaults) {
      String pluginName = ConfigValue.create("Plugin.Name", defaults.getName(), "Localized plugin name. It's used in messages and with internal placeholders.")
         .read(config);
      String pluginPrefix = ConfigProperty.of(ConfigTypes.STRING_OR_EMPTY, "Plugin.Prefix", defaults.getPrefix(), "Plugin prefix. Used in messages.")
         .resolveWithDefaults(config);
      String[] commandAliases = ConfigValue.create(
            "Plugin.Command_Aliases",
            defaults.getCommandAliases(),
            "Command names that will be registered as main plugin commands.",
            "Do not leave this empty. Split multiple names with a comma."
         )
         .read(config);
      String languageCode = ConfigValue.create(
            "Plugin.Language",
            defaults.getLanguage(),
            "Sets the plugin language.",
            "Basically it tells the plugin to use certain messages config from the '/lang/' sub-folder.",
            "If specified language is not available, default one (English) will be used instead.",
            "[Default is System Locale]"
         )
         .read(config);
      DatabaseConfig dataConfig = null;
      if (plugin instanceof NightDataPlugin) {
         plugin.info("Read database configuration...");
         dataConfig = DatabaseConfig.read(plugin);
      }

      return new PluginDetails(pluginName, pluginPrefix, commandAliases, languageCode)
         .setDatabaseConfig(dataConfig)
         .setConfigClass(defaults.getConfigClass())
         .setLangClass(defaults.getLangClass())
         .setPermissionsClass(defaults.getPermissionsClass());
   }

   @Override
   public void write(@NonNull FileConfig config, @NonNull String path) {
      config.set(path + ".Plugin.Name", this.name);
      config.set(path + ".Plugin.Prefix", this.prefix);
      config.setStringArray(path + ".Plugin.Command_Aliases", this.commandAliases);
      config.set(path + ".Plugin.Language", this.language);
   }

   @NonNull
   public String getName() {
      return this.name;
   }

   @NonNull
   public String getPrefix() {
      return this.prefix;
   }

   public String[] getCommandAliases() {
      return this.commandAliases;
   }

   @NonNull
   public String getLanguage() {
      return this.language;
   }

   @Deprecated
   @Nullable
   public DatabaseConfig getDatabaseConfig() {
      return this.databaseConfig;
   }

   @Deprecated
   @NonNull
   public PluginDetails setDatabaseConfig(@Nullable DatabaseConfig databaseConfig) {
      this.databaseConfig = databaseConfig;
      return this;
   }

   @Deprecated
   @Nullable
   public Class<?> getConfigClass() {
      return this.configClass;
   }

   @Deprecated
   public PluginDetails setConfigClass(@Nullable Class<?> configClass) {
      this.configClass = configClass;
      return this;
   }

   @Deprecated
   @Nullable
   public Class<?> getLangClass() {
      return this.langClass;
   }

   @Deprecated
   public PluginDetails setLangClass(@Nullable Class<?> langClass) {
      this.langClass = langClass;
      return this;
   }

   @Deprecated
   @Nullable
   public Class<?> getPermissionsClass() {
      return this.permissionsClass;
   }

   @Deprecated
   public PluginDetails setPermissionsClass(@Nullable Class<?> permissionsClass) {
      this.permissionsClass = permissionsClass;
      return this;
   }
}
