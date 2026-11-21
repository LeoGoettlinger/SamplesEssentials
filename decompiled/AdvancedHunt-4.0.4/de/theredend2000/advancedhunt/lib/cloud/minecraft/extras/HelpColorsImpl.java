package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextColor;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
)
@Generated(
   from = "MinecraftHelp.HelpColors",
   generator = "Immutables"
)
final class HelpColorsImpl implements MinecraftHelp.HelpColors {
   @NonNull
   private final TextColor primary;
   @NonNull
   private final TextColor highlight;
   @NonNull
   private final TextColor alternateHighlight;
   @NonNull
   private final TextColor text;
   @NonNull
   private final TextColor accent;

   private HelpColorsImpl(
      @NonNull TextColor primary, @NonNull TextColor highlight, @NonNull TextColor alternateHighlight, @NonNull TextColor text, @NonNull TextColor accent
   ) {
      this.primary = Objects.requireNonNull(primary, "primary");
      this.highlight = Objects.requireNonNull(highlight, "highlight");
      this.alternateHighlight = Objects.requireNonNull(alternateHighlight, "alternateHighlight");
      this.text = Objects.requireNonNull(text, "text");
      this.accent = Objects.requireNonNull(accent, "accent");
   }

   private HelpColorsImpl(
      HelpColorsImpl original,
      @NonNull TextColor primary,
      @NonNull TextColor highlight,
      @NonNull TextColor alternateHighlight,
      @NonNull TextColor text,
      @NonNull TextColor accent
   ) {
      this.primary = primary;
      this.highlight = highlight;
      this.alternateHighlight = alternateHighlight;
      this.text = text;
      this.accent = accent;
   }

   @NonNull
   @Override
   public TextColor primary() {
      return this.primary;
   }

   @NonNull
   @Override
   public TextColor highlight() {
      return this.highlight;
   }

   @NonNull
   @Override
   public TextColor alternateHighlight() {
      return this.alternateHighlight;
   }

   @NonNull
   @Override
   public TextColor text() {
      return this.text;
   }

   @NonNull
   @Override
   public TextColor accent() {
      return this.accent;
   }

   public final HelpColorsImpl withPrimary(TextColor value) {
      if (this.primary == value) {
         return this;
      } else {
         TextColor newValue = Objects.requireNonNull(value, "primary");
         return new HelpColorsImpl(this, newValue, this.highlight, this.alternateHighlight, this.text, this.accent);
      }
   }

   public final HelpColorsImpl withHighlight(TextColor value) {
      if (this.highlight == value) {
         return this;
      } else {
         TextColor newValue = Objects.requireNonNull(value, "highlight");
         return new HelpColorsImpl(this, this.primary, newValue, this.alternateHighlight, this.text, this.accent);
      }
   }

   public final HelpColorsImpl withAlternateHighlight(TextColor value) {
      if (this.alternateHighlight == value) {
         return this;
      } else {
         TextColor newValue = Objects.requireNonNull(value, "alternateHighlight");
         return new HelpColorsImpl(this, this.primary, this.highlight, newValue, this.text, this.accent);
      }
   }

   public final HelpColorsImpl withText(TextColor value) {
      if (this.text == value) {
         return this;
      } else {
         TextColor newValue = Objects.requireNonNull(value, "text");
         return new HelpColorsImpl(this, this.primary, this.highlight, this.alternateHighlight, newValue, this.accent);
      }
   }

   public final HelpColorsImpl withAccent(TextColor value) {
      if (this.accent == value) {
         return this;
      } else {
         TextColor newValue = Objects.requireNonNull(value, "accent");
         return new HelpColorsImpl(this, this.primary, this.highlight, this.alternateHighlight, this.text, newValue);
      }
   }

   @Override
   public boolean equals(Object another) {
      return this == another ? true : another instanceof HelpColorsImpl && this.equalsByValue((HelpColorsImpl)another);
   }

   private boolean equalsByValue(HelpColorsImpl another) {
      return this.primary.equals(another.primary)
         && this.highlight.equals(another.highlight)
         && this.alternateHighlight.equals(another.alternateHighlight)
         && this.text.equals(another.text)
         && this.accent.equals(another.accent);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.primary.hashCode();
      h += (h << 5) + this.highlight.hashCode();
      h += (h << 5) + this.alternateHighlight.hashCode();
      h += (h << 5) + this.text.hashCode();
      return h + (h << 5) + this.accent.hashCode();
   }

   @Override
   public String toString() {
      return "HelpColors{primary="
         + this.primary
         + ", highlight="
         + this.highlight
         + ", alternateHighlight="
         + this.alternateHighlight
         + ", text="
         + this.text
         + ", accent="
         + this.accent
         + "}";
   }

   public static HelpColorsImpl of(
      @NonNull TextColor primary, @NonNull TextColor highlight, @NonNull TextColor alternateHighlight, @NonNull TextColor text, @NonNull TextColor accent
   ) {
      return new HelpColorsImpl(primary, highlight, alternateHighlight, text, accent);
   }

   public static HelpColorsImpl copyOf(MinecraftHelp.HelpColors instance) {
      return instance instanceof HelpColorsImpl
         ? (HelpColorsImpl)instance
         : of(instance.primary(), instance.highlight(), instance.alternateHighlight(), instance.text(), instance.accent());
   }
}
