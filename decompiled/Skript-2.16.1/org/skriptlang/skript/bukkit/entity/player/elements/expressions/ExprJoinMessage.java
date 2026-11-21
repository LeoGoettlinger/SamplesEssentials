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
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Join Message")
@Description({"The message sent to all online players when a player joins the server."})
@Example.Examples({@Example("on first join:\n\tset the join message to \"Welcome %player% to our awesome server!\"\n"), @Example("on join:\n\tplayer has played before\n\tset the join message to \"Welcome back, %player%!\"\n")})
@Since({"1.4.9"})
@Events({"join"})
public class ExprJoinMessage extends SimpleExpression<Component> implements EventRestrictedSyntax {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprJoinMessage.class, Component.class
                     )
                     .supplier(ExprJoinMessage::new))
                  .priority(SyntaxInfo.SIMPLE))
               .addPattern("[the] (join|log[ ]in)( |-)message"))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Nullable
   protected Component[] get(Event event) {
      if (event instanceof PlayerJoinEvent joinEvent) {
         Component message = joinEvent.joinMessage();
         if (message != null) {
            return new Component[]{message};
         }
      }

      return new Component[0];
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
      if (event instanceof PlayerJoinEvent joinEvent) {
         joinEvent.joinMessage((Component)(delta == null ? Component.empty() : (Component)delta[0]));
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
      return "the join message";
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return new Class[]{PlayerJoinEvent.class};
   }
}
