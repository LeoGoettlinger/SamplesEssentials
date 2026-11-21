package su.nightexpress.nightcore.util.text.tag.impl;

import java.awt.Color;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.tag.api.ContentTag;
import su.nightexpress.nightcore.util.text.tag.api.Tag;
import su.nightexpress.nightcore.util.text.tag.decorator.BaseColorDecorator;

@Deprecated
public class ShortHexColorTag extends Tag implements ContentTag {
   public static final String NAME = "#";

   public ShortHexColorTag() {
      super("#");
   }

   @Nullable
   public BaseColorDecorator parse(@NonNull String tagContent) {
      if (tagContent.length() < 7) {
         return null;
      } else {
         Color color = ParserUtils.colorFromHexString(tagContent);
         return new BaseColorDecorator(color);
      }
   }
}
