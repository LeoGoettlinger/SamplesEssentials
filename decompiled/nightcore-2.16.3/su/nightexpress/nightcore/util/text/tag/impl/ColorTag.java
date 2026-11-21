package su.nightexpress.nightcore.util.text.tag.impl;

import java.awt.Color;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.tag.TagUtils;
import su.nightexpress.nightcore.util.text.tag.api.SimpleTag;
import su.nightexpress.nightcore.util.text.tag.decorator.ColorDecorator;

@Deprecated
public class ColorTag extends SimpleTag implements ColorDecorator {
   private final Color color;

   public ColorTag(@NonNull String name, @NonNull String hex) {
      this(name, new String[0], hex);
   }

   public ColorTag(@NonNull String name, String[] aliases, @NonNull String hex) {
      this(name, aliases, ParserUtils.colorFromHexString(hex));
   }

   @Deprecated
   public ColorTag(@NonNull Color color) {
      this(color, new String[0]);
   }

   @Deprecated
   public ColorTag(@NonNull Color color, String[] aliases) {
      this(ParserUtils.colorToHexString(color).substring(1), aliases, color);
   }

   public ColorTag(@NonNull String name, @NonNull Color color) {
      this(name, new String[0], color);
   }

   public ColorTag(@NonNull String name, String[] aliases, @NonNull Color color) {
      super(name, aliases);
      this.color = color;
   }

   @NonNull
   public Color getColor() {
      return this.color;
   }

   @NonNull
   public String getHex() {
      return ParserUtils.colorToHexString(this.color);
   }

   @Deprecated
   @NonNull
   public String wrapHex(@NonNull String string) {
      String hex = this.getHex();
      return TagUtils.brackets(hex) + string + TagUtils.closedBrackets(hex);
   }

   @NonNull
   @Override
   public NightComponent decorate(@NonNull NightComponent component) {
      return component.color(this.color);
   }

   @Deprecated
   @NonNull
   public String encloseHex(@NonNull String string) {
      return this.wrapHex(string);
   }

   @Deprecated
   @NonNull
   public String toHexString() {
      return this.getHex();
   }
}
