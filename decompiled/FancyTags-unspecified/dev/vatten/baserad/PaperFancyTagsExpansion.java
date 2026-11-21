package dev.vatten.baserad;

import java.util.List;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaperFancyTagsExpansion extends PlaceholderExpansion {
   private final Plugin plugin;

   public PaperFancyTagsExpansion(Plugin plugin) {
      this.plugin = plugin;
   }

   @NotNull
   public String getIdentifier() {
      return "tags";
   }

   @NotNull
   public String getAuthor() {
      return "SampleCraft";
   }

   @NotNull
   public String getVersion() {
      return this.plugin.getPluginInfo().version();
   }

   public boolean persist() {
      return true;
   }

   @NotNull
   public List<String> getPlaceholders() {
      return this.plugin.getTagStore().getTagNames();
   }

   @Nullable
   public String onPlaceholderRequest(Player player, @NotNull String params) {
      return this.plugin.getTagStore().getTagNames().contains(params)
         ? (String)this.plugin.getMiniMessage().serialize(this.plugin.getTagStore().getTag(params).asComponent())
         : null;
   }
}
