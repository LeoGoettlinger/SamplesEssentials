package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

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
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Chat Message")
@Description({"The chat message in a chat event."})
@Example("on chat:\n\tplayer has permission \"admin\"\n\tset the message to \"<light red>%message%\"\n")
@Since({"1.4.6, 2.15 (support for reset)"})
@Events({"chat"})
public class ExprChatMessage extends SimpleExpression<Component> implements EventRestrictedSyntax {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprChatMessage.class, Component.class
                  )
                  .supplier(ExprChatMessage::new))
               .addPattern("[the] [chat( |-)]message"))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   protected Component[] get(Event event) {
      return event instanceof AsyncChatEvent asyncChatEvent ? new Component[]{asyncChatEvent.message()} : new Component[0];
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.getParser().getHasDelayBefore().isTrue()) {
         Skript.error("'" + this.toString(null, false) + "' can't be changed after the event has passed");
         return null;
      } else {
         return switch (mode) {
            case SET, DELETE, RESET -> (Class[])CollectionUtils.array(Component.class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof AsyncChatEvent asyncChatEvent) {
         asyncChatEvent.message((Component)(switch (mode) {
            case SET -> {
               assert delta != null;

               yield (Component)delta[0];
            }
            case DELETE -> Component.empty();
            case RESET -> asyncChatEvent.originalMessage();
            default -> throw new IllegalStateException("Unexpected change mode: " + mode);
         }));
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
      return "the chat message";
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return new Class[]{AsyncChatEvent.class};
   }
}
