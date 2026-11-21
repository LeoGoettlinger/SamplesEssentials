package su.nightexpress.nightcore.util.text.night.tag.handler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.night.entry.EntryGroup;

public class KeybindTagHandler extends ClassicTagHandler {
   @Override
   protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
      if (tagContent != null) {
         String key = ParserUtils.unquoted(tagContent);
         group.appendKeybindEntry(key);
      }
   }

   @Override
   protected void onHandleClose(@NonNull EntryGroup group) {
   }

   @Override
   public boolean canBeClosed() {
      return false;
   }
}
