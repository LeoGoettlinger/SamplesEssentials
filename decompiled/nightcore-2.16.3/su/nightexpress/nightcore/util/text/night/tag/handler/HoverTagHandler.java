package su.nightexpress.nightcore.util.text.night.tag.handler;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.text.event.NightHoverEvent;
import su.nightexpress.nightcore.util.Strings;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.nbt.NbtUtil;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.night.TextParser;
import su.nightexpress.nightcore.util.text.night.entry.EntryGroup;
import su.nightexpress.nightcore.util.text.night.tag.TagContent;

public class HoverTagHandler extends ClassicTagHandler {
   @Override
   protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
      if (tagContent != null) {
         TagContent content = ParserUtils.parseInnerContent(tagContent);
         String value = content.second();
         if (value != null) {
            NightHoverEvent.Action<?> action = NightHoverEvent.Action.byName(content.first());
            if (action != null) {
               NightHoverEvent<?> hoverEvent;
               if (action == NightHoverEvent.Action.SHOW_TEXT) {
                  hoverEvent = NightHoverEvent.showText(TextParser.parse(value));
               } else {
                  if (action != NightHoverEvent.Action.SHOW_ITEM) {
                     return;
                  }

                  String tag = Strings.fromBase64(value);
                  ItemStack itemStack = NbtUtil.tagToItemStack(tag, Version.getCurrent().getDataVersion());
                  if (itemStack == null) {
                     return;
                  }

                  hoverEvent = NightHoverEvent.showItem(itemStack);
               }

               group.setStyle(style -> style.hoverEvent(hoverEvent));
            }
         }
      }
   }

   @Override
   protected void onHandleClose(@NonNull EntryGroup group) {
   }
}
