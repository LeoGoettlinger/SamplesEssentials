package su.nightexpress.nightcore.integration.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.placeholder.PlaceholderRegistry;

public class PluginExpansion extends PlaceholderExpansion implements Expansion {
   private final JavaPlugin plugin;
   private final PlaceholderRegistry registry;
   private final String identifier;

   public PluginExpansion(@NonNull JavaPlugin plugin, @NonNull PlaceholderRegistry registry, @NonNull String identifier) {
      this.plugin = plugin;
      this.registry = registry;
      this.identifier = identifier;
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return this.identifier;
   }

   @NonNull
   @Override
   public String getAuthor() {
      return (String)this.plugin.getDescription().getAuthors().getFirst();
   }

   @NonNull
   @Override
   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   @Override
   public boolean persist() {
      return true;
   }

   @Nullable
   @Override
   public String onPlaceholderRequest(Player player, @NonNull String params) {
      if (player == null) {
         return null;
      } else {
         return !this.registry.isEmpty() ? this.registry.onPlaceholderRequest(player, params) : super.onPlaceholderRequest(player, params);
      }
   }
}
