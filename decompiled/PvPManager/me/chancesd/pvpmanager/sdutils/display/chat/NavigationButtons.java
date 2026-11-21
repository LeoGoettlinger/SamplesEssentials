package me.chancesd.pvpmanager.sdutils.display.chat;

import org.jetbrains.annotations.NotNull;

public class NavigationButtons {
   private final String previousText;
   private final String nextText;
   private final String navigationPrefix;

   private NavigationButtons(String previousText, String nextText, String navigationPrefix) {
      this.previousText = previousText;
      this.nextText = nextText;
      this.navigationPrefix = navigationPrefix;
   }

   @NotNull
   public String getNavigationPrefix() {
      return this.navigationPrefix;
   }

   public String generatePrevious(int previousPage) {
      return this.previousText.replace("{page}", String.valueOf(previousPage));
   }

   public String generateNext(int nextPage) {
      return this.nextText.replace("{page}", String.valueOf(nextPage));
   }

   public static NavigationButtons.Builder builder() {
      return new NavigationButtons.Builder();
   }

   public static class Builder {
      private String previousText = "&7[&c← Prev&7]";
      private String nextText = "&7[&cNext →&7]";
      private String navigationPrefix = "/chatmenu";

      public NavigationButtons.Builder previousText(String text) {
         this.previousText = text;
         return this;
      }

      public NavigationButtons.Builder nextText(String text) {
         this.nextText = text;
         return this;
      }

      public NavigationButtons.Builder navigationPrefix(String prefix) {
         this.navigationPrefix = prefix;
         return this;
      }

      public NavigationButtons build() {
         return new NavigationButtons(this.previousText, this.nextText, this.navigationPrefix);
      }
   }
}
