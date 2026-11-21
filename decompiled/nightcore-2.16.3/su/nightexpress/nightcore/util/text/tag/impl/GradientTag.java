package su.nightexpress.nightcore.util.text.tag.impl;

import java.awt.Color;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.tag.TagUtils;
import su.nightexpress.nightcore.util.text.tag.api.ContentTag;
import su.nightexpress.nightcore.util.text.tag.api.Tag;
import su.nightexpress.nightcore.util.text.tag.decorator.GradientColorDecorator;

@Deprecated
public class GradientTag extends Tag implements ContentTag {
   public static final String NAME = "gradient";

   public GradientTag() {
      super("gradient");
   }

   @Deprecated
   @NonNull
   public String enclose(@NonNull String hexStart, @NonNull String hexEnd, @NonNull String text) {
      return this.wrap(text, hexStart, hexEnd);
   }

   @Deprecated
   @NonNull
   public String wrap(@NonNull String string, @NonNull String fromHex, @NonNull String toHex) {
      return this.wrap(string, fromHex, toHex);
   }

   @NonNull
   public String wrap(@NonNull String string, Color... colors) {
      return this.wrap(string, Stream.of(colors).map(ParserUtils::colorToHexString).toArray(String[]::new));
   }

   @NonNull
   public String wrap(@NonNull String string, String... hexCodes) {
      String joined = String.join(String.valueOf(':'), hexCodes);
      String tagOpen = TagUtils.brackets(this.getName() + ":" + joined);
      String tagClose = this.getClosingName();
      return tagOpen + string + tagClose;
   }

   @Nullable
   public GradientColorDecorator parse(@NonNull String content) {
      String[] split = content.split(String.valueOf(':'));
      int length = split.length;
      if (length < 2) {
         return null;
      } else {
         Color[] colors = new Color[length];

         for (int index = 0; index < length; index++) {
            Color stop = ParserUtils.colorFromSchemeOrHex(split[index]);
            if (stop != null) {
               colors[index] = stop;
            }
         }

         return new GradientColorDecorator(colors);
      }
   }
}
