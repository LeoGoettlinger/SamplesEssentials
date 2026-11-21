package su.nightexpress.nightcore.util.text.night.tag.handler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.night.entry.EntryGroup;

public class InsertionTagHandler extends ClassicTagHandler {
   @Override
   protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
      if (tagContent != null) {
         String insetion = ParserUtils.unquoted(tagContent);
         group.setStyle(style -> style.insertion(insetion));
      }
   }

   @Override
   protected void onHandleClose(@NonNull EntryGroup group) {
   }
}
