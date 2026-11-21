package su.nightexpress.nightcore.util.text.night.tag.handler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.night.entry.EntryGroup;

public class LangTagHandler extends ClassicTagHandler {
   @Override
   protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
      if (tagContent != null) {
         String content = fixContent(tagContent);
         int index = ParserUtils.findUnescapedUnquotedChar(content, ':', 0);
         String key = ParserUtils.unquoted(index < 0 ? content : content.substring(0, index));
         String fallback = index >= 0 && index < content.length() ? ParserUtils.unquoted(content.substring(index + 1)) : null;
         group.appendLangEntry(key, fallback);
      }
   }

   @NonNull
   private static String fixContent(@NonNull String tagContent) {
      return tagContent.endsWith("/") ? tagContent.substring(0, tagContent.length() - 1) : tagContent;
   }

   @Override
   protected void onHandleClose(@NonNull EntryGroup group) {
   }

   @Override
   public boolean canBeClosed() {
      return false;
   }
}
