package dev.vatten.baserad;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;

public class TextFormatter {
   private final Component symbol;
   private final Style contentStyle;
   public static TextFormatter INFO = builder().styleContent(Style.style(TextColor.color(14548991))).build();
   public static TextFormatter WARN = builder()
      .symbol("\ud83d\udd14")
      .styleSymbol(Style.style(TextColor.color(15901511)))
      .styleContent(Style.style(TextColor.color(16766282)))
      .build();
   public static TextFormatter SEVERE = builder()
      .symbol("⚠")
      .styleSymbol(Style.style(TextColor.color(12918286)))
      .styleContent(Style.style(TextColor.color(16728095)))
      .build();
   public static TextFormatter SUCCESS = builder()
      .symbol("✔")
      .styleSymbol(Style.style(TextColor.color(1170980)))
      .styleContent(Style.style(TextColor.color(4915051)))
      .build();
   public static TextFormatter CASUAL = builder().styleContent(Style.style(NamedTextColor.GRAY)).build();
   public static TextFormatter RESET = builder().styleContent(Style.empty().color(TextColor.color(16777215))).build();

   TextFormatter(Component symbol, Style contentStyle) {
      this.symbol = symbol;
      this.contentStyle = contentStyle;
   }

   public Component format(String text) {
      return this.format(Component.text(text));
   }

   public Component format(Component component) {
      return this.symbol.append(component.applyFallbackStyle(this.contentStyle));
   }

   public static TextFormatter.Builder builder() {
      return new TextFormatter.Builder();
   }

   public static class Builder {
      private String symbol = "";
      private Style symbolStyle = Style.empty();
      private Style contentStyle = Style.empty();

      public TextFormatter.Builder symbol(String symbol) {
         this.symbol = symbol;
         return this;
      }

      public TextFormatter.Builder styleSymbol(Style symbolStyle) {
         this.symbolStyle = symbolStyle;
         return this;
      }

      public TextFormatter.Builder styleContent(Style contentStyle) {
         this.contentStyle = contentStyle;
         return this;
      }

      public TextFormatter build() {
         return new TextFormatter(
            (Component)(this.symbol.isEmpty() ? Component.empty() : Component.text(this.symbol, this.symbolStyle).appendSpace()), this.contentStyle
         );
      }
   }
}
