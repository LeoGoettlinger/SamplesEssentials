package org.skriptlang.skript.bukkit.misc.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Broadcast Message")
@Description({"The message broadcasted in a broadcast event."})
@Example("on broadcast:\n\tset broadcast message to \"<light green>[BROADCAST] %broadcast message%\"\n")
@Since({"2.10"})
@Events({"broadcast"})
public class ExprBroadcastMessage extends SimpleExpression<Component> implements EventRestrictedSyntax {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprBroadcastMessage.class, Component.class
                     )
                     .supplier(ExprBroadcastMessage::new))
                  .priority(SyntaxInfo.SIMPLE))
               .addPattern("[the] broadcast(-|[ed] )message"))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Nullable
   protected Component[] get(Event event) {
      return event instanceof BroadcastMessageEvent broadcastEvent ? new Component[]{broadcastEvent.message()} : new Component[0];
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.getParser().getHasDelayBefore().isTrue()) {
         Skript.error("'" + this.toString(null, false) + "' can't be changed after the event has passed");
         return null;
      } else {
         return switch (mode) {
            case SET, DELETE -> (Class[])CollectionUtils.array(Component.class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof BroadcastMessageEvent broadcastEvent) {
         broadcastEvent.message((Component)(delta == null ? Component.empty() : (Component)delta[0]));
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Component> getReturnType() {
      return Component.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the broadcast message";
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return new Class[]{BroadcastMessageEvent.class};
   }
}
