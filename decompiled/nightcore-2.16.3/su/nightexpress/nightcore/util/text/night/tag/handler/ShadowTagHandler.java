package su.nightexpress.nightcore.util.text.night.tag.handler;

import java.awt.Color;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.Numbers;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.night.entry.EntryGroup;
import su.nightexpress.nightcore.util.text.night.tag.TagContent;

public class ShadowTagHandler extends ClassicTagHandler {
   private static final float DEFAULT_ALPHA = 0.25F;
   private static final float MAX_ALPHA = 1.0F;

   @Override
   protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
      if (tagContent != null && tagContent.length() >= 7) {
         TagContent content = ParserUtils.parseInnerContent(tagContent);
         String alphaRaw = content.second();
         Color color = ParserUtils.colorFromSchemeOrHex(content.first());
         if (color != null) {
            float[] rgb = color.getRGBColorComponents(null);
            float alpha = alphaRaw == null ? 0.25F : Math.min(1.0F, Numbers.getFloatAbs(alphaRaw));
            group.setStyle(nightStyle -> nightStyle.shadowColor(new Color(rgb[0], rgb[1], rgb[2], alpha)));
         }
      }
   }

   @Override
   protected void onHandleClose(@NonNull EntryGroup group) {
   }
}
