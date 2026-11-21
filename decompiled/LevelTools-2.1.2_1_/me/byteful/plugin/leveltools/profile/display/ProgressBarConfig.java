package me.byteful.plugin.leveltools.profile.display;

import org.jetbrains.annotations.NotNull;

public final class ProgressBarConfig {
   private final int totalBars;
   private final char barSymbol;
   private final char prefixSymbol;
   private final char suffixSymbol;
   private final char prefixColor;
   private final char suffixColor;
   private final char completedColor;
   private final char placeholderColor;

   public ProgressBarConfig(
      int totalBars, char barSymbol, char prefixSymbol, char suffixSymbol, char prefixColor, char suffixColor, char completedColor, char placeholderColor
   ) {
      this.totalBars = totalBars;
      this.barSymbol = barSymbol;
      this.prefixSymbol = prefixSymbol;
      this.suffixSymbol = suffixSymbol;
      this.prefixColor = prefixColor;
      this.suffixColor = suffixColor;
      this.completedColor = completedColor;
      this.placeholderColor = placeholderColor;
   }

   @NotNull
   public static ProgressBarConfig defaultConfig() {
      return new ProgressBarConfig(50, '|', '[', ']', '8', '8', 'e', '7');
   }

   public static ProgressBarConfig.Builder builder() {
      return new ProgressBarConfig.Builder();
   }

   public int getTotalBars() {
      return this.totalBars;
   }

   public char getBarSymbol() {
      return this.barSymbol;
   }

   public char getPrefixSymbol() {
      return this.prefixSymbol;
   }

   public char getSuffixSymbol() {
      return this.suffixSymbol;
   }

   public char getPrefixColor() {
      return this.prefixColor;
   }

   public char getSuffixColor() {
      return this.suffixColor;
   }

   public char getCompletedColor() {
      return this.completedColor;
   }

   public char getPlaceholderColor() {
      return this.placeholderColor;
   }

   @NotNull
   public String buildProgressBar(double currentXp, double maxXp) {
      int completedBars = (int)Math.round(currentXp / maxXp * this.totalBars);
      completedBars = Math.min(completedBars, this.totalBars);
      StringBuilder bar = new StringBuilder();
      bar.append("&").append(this.prefixColor).append(this.prefixSymbol);
      bar.append("&").append(this.completedColor);

      for (int i = 0; i < completedBars; i++) {
         bar.append(this.barSymbol);
      }

      bar.append("&").append(this.placeholderColor);

      for (int i = completedBars; i < this.totalBars; i++) {
         bar.append(this.barSymbol);
      }

      bar.append("&").append(this.suffixColor).append(this.suffixSymbol);
      return bar.toString();
   }

   public static final class Builder {
      private int totalBars = 50;
      private char barSymbol = '|';
      private char prefixSymbol = '[';
      private char suffixSymbol = ']';
      private char prefixColor = '8';
      private char suffixColor = '8';
      private char completedColor = 'e';
      private char placeholderColor = '7';

      public ProgressBarConfig.Builder totalBars(int totalBars) {
         this.totalBars = totalBars;
         return this;
      }

      public ProgressBarConfig.Builder barSymbol(char barSymbol) {
         this.barSymbol = barSymbol;
         return this;
      }

      public ProgressBarConfig.Builder prefixSymbol(char prefixSymbol) {
         this.prefixSymbol = prefixSymbol;
         return this;
      }

      public ProgressBarConfig.Builder suffixSymbol(char suffixSymbol) {
         this.suffixSymbol = suffixSymbol;
         return this;
      }

      public ProgressBarConfig.Builder prefixColor(char prefixColor) {
         this.prefixColor = prefixColor;
         return this;
      }

      public ProgressBarConfig.Builder suffixColor(char suffixColor) {
         this.suffixColor = suffixColor;
         return this;
      }

      public ProgressBarConfig.Builder completedColor(char completedColor) {
         this.completedColor = completedColor;
         return this;
      }

      public ProgressBarConfig.Builder placeholderColor(char placeholderColor) {
         this.placeholderColor = placeholderColor;
         return this;
      }

      public ProgressBarConfig build() {
         return new ProgressBarConfig(
            this.totalBars,
            this.barSymbol,
            this.prefixSymbol,
            this.suffixSymbol,
            this.prefixColor,
            this.suffixColor,
            this.completedColor,
            this.placeholderColor
         );
      }
   }
}
