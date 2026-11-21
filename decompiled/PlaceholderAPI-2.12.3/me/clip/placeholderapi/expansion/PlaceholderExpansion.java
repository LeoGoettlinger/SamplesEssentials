package me.clip.placeholderapi.expansion;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public abstract class PlaceholderExpansion extends PlaceholderHook {
   @Internal
   protected PlaceholderExpansion.Type expansionType = PlaceholderExpansion.Type.INTERNAL;

   @NotNull
   public abstract String getIdentifier();

   @NotNull
   public abstract String getAuthor();

   @NotNull
   public abstract String getVersion();

   @NotNull
   public String getName() {
      return this.getIdentifier();
   }

   @Nullable
   public String getRequiredPlugin() {
      return this.getPlugin();
   }

   @NotNull
   public List<String> getPlaceholders() {
      return Collections.emptyList();
   }

   public boolean persist() {
      return false;
   }

   public final boolean isRegistered() {
      return this.getPlaceholderAPI().getLocalExpansionManager().findExpansionByIdentifier(this.getIdentifier()).map(it -> it.equals(this)).orElse(false);
   }

   public boolean canRegister() {
      return this.getRequiredPlugin() == null || Bukkit.getPluginManager().getPlugin(this.getRequiredPlugin()) != null;
   }

   public boolean register() {
      return this.getPlaceholderAPI().getLocalExpansionManager().register(this);
   }

   public final boolean unregister() {
      return this.getPlaceholderAPI().getLocalExpansionManager().unregister(this);
   }

   @NotNull
   public final PlaceholderAPIPlugin getPlaceholderAPI() {
      return PlaceholderAPIPlugin.getInstance();
   }

   @Internal
   public PlaceholderExpansion.Type getExpansionType() {
      return this.expansionType;
   }

   @Internal
   public void setExpansionType(PlaceholderExpansion.Type expansionType) {
      this.expansionType = expansionType;
   }

   @Nullable
   public final ConfigurationSection getConfigSection() {
      return this.getPlaceholderAPI().getConfig().getConfigurationSection("expansions." + this.getIdentifier());
   }

   @Nullable
   public final ConfigurationSection getConfigSection(@NotNull String path) {
      ConfigurationSection section = this.getConfigSection();
      return section == null ? null : section.getConfigurationSection(path);
   }

   @Nullable
   @Contract("_, !null -> !null")
   public final Object get(@NotNull String path, Object def) {
      ConfigurationSection section = this.getConfigSection();
      return section == null ? def : section.get(path, def);
   }

   public final int getInt(@NotNull String path, int def) {
      ConfigurationSection section = this.getConfigSection();
      return section == null ? def : section.getInt(path, def);
   }

   public final long getLong(@NotNull String path, long def) {
      ConfigurationSection section = this.getConfigSection();
      return section == null ? def : section.getLong(path, def);
   }

   public final double getDouble(@NotNull String path, double def) {
      ConfigurationSection section = this.getConfigSection();
      return section == null ? def : section.getDouble(path, def);
   }

   @Nullable
   @Contract("_, !null -> !null")
   public final String getString(@NotNull String path, @Nullable String def) {
      ConfigurationSection section = this.getConfigSection();
      return section == null ? def : section.getString(path, def);
   }

   @NotNull
   public final List<String> getStringList(@NotNull String path) {
      ConfigurationSection section = this.getConfigSection();
      return section == null ? Collections.emptyList() : section.getStringList(path);
   }

   public final boolean getBoolean(@NotNull String path, boolean def) {
      ConfigurationSection section = this.getConfigSection();
      return section == null ? def : section.getBoolean(path, def);
   }

   public final boolean configurationContains(@NotNull String path) {
      ConfigurationSection section = this.getConfigSection();
      return section != null && section.contains(path);
   }

   public void log(Level level, String msg) {
      this.getPlaceholderAPI().getLogger().log(level, "[" + this.getName() + "] " + msg);
   }

   public void log(Level level, String msg, Throwable throwable) {
      this.getPlaceholderAPI().getLogger().log(level, "[" + this.getName() + "] " + msg, throwable);
   }

   public void info(String msg) {
      this.log(Level.INFO, msg);
   }

   public void warning(String msg) {
      this.log(Level.WARNING, msg);
   }

   public void severe(String msg) {
      this.log(Level.SEVERE, msg);
   }

   public void severe(String msg, Throwable throwable) {
      this.log(Level.SEVERE, msg, throwable);
   }

   @Override
   public final boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof PlaceholderExpansion)) {
         return false;
      } else {
         PlaceholderExpansion expansion = (PlaceholderExpansion)o;
         return this.getIdentifier().equals(expansion.getIdentifier())
            && this.getAuthor().equals(expansion.getAuthor())
            && this.getVersion().equals(expansion.getVersion());
      }
   }

   @Override
   public final String toString() {
      return String.format(
         "PlaceholderExpansion[name: '%s', author: '%s', version: '%s', type: '%s']",
         this.getName(),
         this.getAuthor(),
         this.getVersion(),
         this.getExpansionType()
      );
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public String getPlugin() {
      return null;
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public String getDescription() {
      return null;
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "2.13.0"
   )
   public String getLink() {
      return null;
   }

   public static enum Type {
      INTERNAL,
      EXTERNAL;
   }
}
