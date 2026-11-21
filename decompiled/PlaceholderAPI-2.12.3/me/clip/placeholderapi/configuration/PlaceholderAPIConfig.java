package me.clip.placeholderapi.configuration;

import java.util.Optional;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.jetbrains.annotations.NotNull;

public final class PlaceholderAPIConfig {
   @NotNull
   private final PlaceholderAPIPlugin plugin;

   public PlaceholderAPIConfig(@NotNull PlaceholderAPIPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean checkUpdates() {
      return this.plugin.getConfig().getBoolean("check_updates");
   }

   public boolean isCloudEnabled() {
      return this.plugin.getConfig().getBoolean("cloud_enabled");
   }

   public void setCloudEnabled(boolean state) {
      this.plugin.getConfig().set("cloud_enabled", state);
      this.plugin.saveConfig();
   }

   public boolean isDebugMode() {
      return this.plugin.getConfig().getBoolean("debug", false);
   }

   public boolean useAdventureReplacer() {
      return this.plugin.getConfig().getBoolean("use_adventure_provided_replacer", false);
   }

   public Optional<ExpansionSort> getExpansionSort() {
      String option = this.plugin.getConfig().getString("cloud_sorting", ExpansionSort.LATEST.name());

      try {
         return Optional.of(ExpansionSort.valueOf(option.toUpperCase()));
      } catch (IllegalArgumentException var3) {
         return Optional.empty();
      }
   }

   @NotNull
   public String dateFormat() {
      return this.plugin.getConfig().getString("date_format", "MM/dd/yy HH:mm:ss");
   }

   @NotNull
   public String booleanTrue() {
      return this.plugin.getConfig().getString("boolean.true", "true");
   }

   @NotNull
   public String booleanFalse() {
      return this.plugin.getConfig().getString("boolean.false", "false");
   }

   public boolean useAdventureProvidedReplacer() {
      return this.plugin.getConfig().getBoolean("use_adventure_provided_replacer", false);
   }

   public boolean detectMaliciousExpansions() {
      return this.plugin.getConfig().getBoolean("detect_malicious_expansions", true);
   }
}
