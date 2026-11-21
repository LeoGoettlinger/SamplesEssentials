package su.nightexpress.nightcore.util.text.tag.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.text.NightTextDecoration;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.tag.api.SimpleTag;
import su.nightexpress.nightcore.util.text.tag.decorator.Decorator;

@Deprecated
public class FontStyleTag extends SimpleTag implements Decorator {
   private static final String INVERT_SIGN = "!";
   private final FontStyleTag.Style style;
   private final boolean inverted;

   public FontStyleTag(@NonNull String name, @NonNull FontStyleTag.Style style, boolean inverted) {
      this(name, new String[0], style, inverted);
   }

   public FontStyleTag(@NonNull String name, String[] aliases, @NonNull FontStyleTag.Style style, boolean inverted) {
      super(name, aliases);
      this.style = style;
      this.inverted = inverted;
   }

   @NonNull
   public FontStyleTag inverted() {
      String[] aliases = this.aliases.stream().map(alias -> "!" + alias).toArray(String[]::new);
      return new FontStyleTag(aliases[0], aliases, this.style, true);
   }

   @NonNull
   public FontStyleTag.Style getStyle() {
      return this.style;
   }

   @NonNull
   @Override
   public NightComponent decorate(@NonNull NightComponent component) {
      return switch (this.style) {
         case BOLD -> component.decoration(NightTextDecoration.BOLD, !this.inverted);
         case ITALIC -> component.decoration(NightTextDecoration.ITALIC, !this.inverted);
         case UNDERLINED -> component.decoration(NightTextDecoration.UNDERLINED, !this.inverted);
         case STRIKETHROUGH -> component.decoration(NightTextDecoration.STRIKETHROUGH, !this.inverted);
         case OBFUSCATED -> component.decoration(NightTextDecoration.OBFUSCATED, !this.inverted);
      };
   }

   public static enum Style {
      BOLD,
      ITALIC,
      UNDERLINED,
      STRIKETHROUGH,
      OBFUSCATED;
   }
}
