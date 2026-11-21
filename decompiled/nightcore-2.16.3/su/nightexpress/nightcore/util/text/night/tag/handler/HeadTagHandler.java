package su.nightexpress.nightcore.util.text.night.tag.handler;

import java.util.UUID;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.common.NightKey;
import su.nightexpress.nightcore.bridge.text.contents.NightObjectContents;
import su.nightexpress.nightcore.bridge.text.contents.NightPlayerHeadObjectContents;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.night.entry.EntryGroup;
import su.nightexpress.nightcore.util.text.night.entry.PlayerHeadEntry;
import su.nightexpress.nightcore.util.text.night.tag.TagContent;

public class HeadTagHandler extends ClassicTagHandler {
   private static final Pattern UUIDv4_PATTERN = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ABCD][0-9a-f]{3}-[0-9a-f]{12}", 2);

   @Override
   protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
      if (tagContent != null) {
         if (Version.withCopperAge()) {
            TagContent content = ParserUtils.parseInnerContent(tagContent);
            String data = ParserUtils.unquoted(content.first());
            boolean hat = !content.hasBoth() || Boolean.parseBoolean(content.second());
            NightPlayerHeadObjectContents contents;
            if (UUIDv4_PATTERN.matcher(data).matches()) {
               contents = NightObjectContents.playerHead(UUID.fromString(data), hat);
            } else if (data.contains("/") && NightKey.parseable(data)) {
               contents = NightObjectContents.playerHead(NightKey.key(data), hat);
            } else {
               contents = NightObjectContents.playerHead(data, hat);
            }

            group.appendEntry(new PlayerHeadEntry(group, contents));
         }
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
