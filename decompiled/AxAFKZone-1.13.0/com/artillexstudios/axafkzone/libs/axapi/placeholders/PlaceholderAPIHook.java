package com.artillexstudios.axafkzone.libs.axapi.placeholders;

import com.artillexstudios.axafkzone.libs.axapi.AxPlugin;
import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook extends PlaceholderExpansion {
   private final AxPlugin plugin;

   public PlaceholderAPIHook(AxPlugin plugin) {
      this.plugin = plugin;
   }

   @NotNull
   public String getIdentifier() {
      String identifier = FeatureFlags.PLACEHOLDER_API_IDENTIFIER.get();
      if (identifier.isBlank()) {
         String pluginName = this.plugin.getName().toLowerCase(Locale.ENGLISH);
         LogUtils.error("PlaceholderAPI identifier is not set up! Please set it! Defaulting to {}", pluginName);
         return pluginName;
      } else {
         return identifier;
      }
   }

   @NotNull
   public String getAuthor() {
      return "Artillex-Studios";
   }

   @NotNull
   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   @Nullable
   public String onPlaceholderRequest(Player player, @NotNull String params) {
      if (FeatureFlags.DEBUG.get()) {
         LogUtils.debug("Requesting placeholders for {} user, with parameters: {}!", player, params);
      }

      return PaperPlaceholderHandler.parse("%" + params + "%", Player.class, player);
   }

   @Nullable
   public String onRequest(OfflinePlayer player, @NotNull String params) {
      if (FeatureFlags.DEBUG.get()) {
         LogUtils.debug("Requesting placeholders for {} user, with parameters: {}!", player, params);
      }

      return PaperPlaceholderHandler.parse("%" + params + "%", OfflinePlayer.class, player);
   }

   public boolean persist() {
      return true;
   }

   @NotNull
   public List<String> getPlaceholders() {
      return PaperPlaceholderHandler.placeholders(FeatureFlags.PLACEHOLDER_API_IDENTIFIER.get());
   }
}
