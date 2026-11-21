package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.standard;

import xyz.lychee.lagfixer.libs.kyori.adventure.text.event.ClickEvent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.format.Style;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.Context;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.ParsingException;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.internal.serializer.QuotingOverride;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.internal.serializer.StyleClaim;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.Tag;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

final class ClickTag {
   private static final String CLICK = "click";
   static final TagResolver RESOLVER = SerializableResolver.claimingStyle(
      "click",
      ClickTag::create,
      StyleClaim.claim(
         "click",
         Style::clickEvent,
         (event, emitter) -> emitter.tag("click").argument(ClickEvent.Action.NAMES.key(event.action())).argument(event.value(), QuotingOverride.QUOTED)
      )
   );

   private ClickTag() {
   }

   static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
      String actionName = args.popOr(() -> "A click tag requires an action of one of " + ClickEvent.Action.NAMES.keys()).lowerValue();
      ClickEvent.Action action = ClickEvent.Action.NAMES.value(actionName);
      if (action == null) {
         throw ctx.newException("Unknown click event action '" + actionName + "'", args);
      } else {
         String value = args.popOr("Click event actions require a value").value();
         return Tag.styling(ClickEvent.clickEvent(action, value));
      }
   }
}
