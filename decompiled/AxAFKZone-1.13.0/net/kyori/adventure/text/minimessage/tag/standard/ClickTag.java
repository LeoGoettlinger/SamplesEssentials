package net.kyori.adventure.text.minimessage.tag.standard;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.QuotingOverride;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

final class ClickTag {
   private static final String CLICK = "click";
   static final TagResolver RESOLVER = SerializableResolver.claimingStyle(
      "click",
      ClickTag::create,
      StyleClaim.claim(
         "click",
         Style::clickEvent,
         (event, emitter) -> emitter.tag("click").argument((String)Action.NAMES.key(event.action())).argument(event.value(), QuotingOverride.QUOTED)
      )
   );

   private ClickTag() {
   }

   static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
      String actionName = args.popOr(() -> "A click tag requires an action of one of " + Action.NAMES.keys()).lowerValue();
      Action action = (Action)Action.NAMES.value(actionName);
      if (action == null) {
         throw ctx.newException("Unknown click event action '" + actionName + "'", args);
      } else {
         String value = args.popOr("Click event actions require a value").value();
         return Tag.styling(ClickEvent.clickEvent(action, value));
      }
   }
}
