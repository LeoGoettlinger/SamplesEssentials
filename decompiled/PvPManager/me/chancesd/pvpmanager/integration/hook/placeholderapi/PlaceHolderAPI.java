package me.chancesd.pvpmanager.integration.hook.placeholderapi;

import java.util.List;
import me.chancesd.pvpmanager.PvPManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceHolderAPI extends PlaceholderExpansion {
   private final PvPManager plugin;
   private final PlaceholderProcessor processor;

   public PlaceHolderAPI(PvPManager plugin) {
      this.plugin = plugin;
      this.processor = new PlaceholderProcessor(plugin);
   }

   public String onPlaceholderRequest(Player player, String identifier) {
      return this.processor.processPlaceholder(player, identifier);
   }

   public boolean persist() {
      return true;
   }

   public boolean canRegister() {
      return true;
   }

   public String getAuthor() {
      return this.plugin.getDescription().getAuthors().toString();
   }

   public String getName() {
      return this.plugin.getDescription().getName();
   }

   public String getIdentifier() {
      return "pvpmanager";
   }

   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public List<String> getPlaceholders() {
      return this.processor.getPlaceholders();
   }
}
