package org.lushplugins.lushrewards.hook;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.hook.Hook;

public class PlaceholderAPIHook extends Hook {
   private PlaceholderAPIHook.PlaceholderExpansion expansion;
   private PlaceholderAPIHook.OutdatedPlaceholderExpansion outdatedExpansion;

   public PlaceholderAPIHook() {
      super(HookId.PLACEHOLDER_API.toString());
   }

   @Override
   public void onEnable() {
      this.expansion = new PlaceholderAPIHook.PlaceholderExpansion();
      this.expansion.register();
      this.outdatedExpansion = new PlaceholderAPIHook.OutdatedPlaceholderExpansion();
      this.outdatedExpansion.register();
   }

   @Override
   protected void onDisable() {
      if (this.expansion != null) {
         this.expansion.unregister();
         this.expansion = null;
      }

      if (this.outdatedExpansion != null) {
         this.outdatedExpansion.unregister();
         this.outdatedExpansion = null;
      }
   }

   @Deprecated(
      since = "3.0.0"
   )
   public static class OutdatedPlaceholderExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {
      public String onPlaceholderRequest(Player player, @NotNull String params) {
         return LushRewards.getInstance().getLocalPlaceholders().parsePlaceholder(params, player);
      }

      public boolean persist() {
         return true;
      }

      public boolean canRegister() {
         return true;
      }

      @NotNull
      public String getIdentifier() {
         return "rewarder";
      }

      @NotNull
      public String getAuthor() {
         return LushRewards.getInstance().getDescription().getAuthors().toString();
      }

      @NotNull
      public String getVersion() {
         return LushRewards.getInstance().getDescription().getVersion();
      }
   }

   public static class PlaceholderExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {
      public String onPlaceholderRequest(Player player, @NotNull String params) {
         return LushRewards.getInstance().getLocalPlaceholders().parsePlaceholder(params, player);
      }

      public boolean persist() {
         return true;
      }

      public boolean canRegister() {
         return true;
      }

      @NotNull
      public String getIdentifier() {
         return "lushrewards";
      }

      @NotNull
      public String getAuthor() {
         return LushRewards.getInstance().getDescription().getAuthors().toString();
      }

      @NotNull
      public String getVersion() {
         return LushRewards.getInstance().getDescription().getVersion();
      }
   }
}
