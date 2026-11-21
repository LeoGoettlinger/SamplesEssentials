package org.skriptlang.skript.bukkit.misc.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Message of the Day")
@Description({"The message of the day in the server list.", "This can be changed in a <a href='#server_list_ping'>server list ping</a> event only.", "Use 'default MOTD' to obtain the default MOTD set through the server configuration. This cannot be changed."})
@Example("on server ling ping:\n\tset the motd to \"<red>Join our server today!\"\n")
@Since({"2.3"})
@Keywords({"MOTD"})
@Events({"server list ping"})
public class ExprMOTD extends SimpleExpression<Component> {
   private boolean isDefault;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprMOTD.class, Component.class
                     )
                     .supplier(ExprMOTD::new))
                  .priority(SyntaxInfo.SIMPLE))
               .addPattern("[the] [1:default|2:shown|2:displayed] (MOTD|message of [the] day)"))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      boolean isServerPingEvent = this.getParser().isCurrentEvent(PaperServerListPingEvent.class);
      if (parseResult.mark == 2 && !isServerPingEvent) {
         Skript.error("The 'shown' MOTD expression can't be used outside of a server list ping event");
         return false;
      } else {
         this.isDefault = !isServerPingEvent && parseResult.mark == 0 || parseResult.mark == 1;
         return true;
      }
   }

   public Component[] get(Event event) {
      if (this.isDefault) {
         return new Component[]{Bukkit.motd()};
      } else {
         return event instanceof ServerListPingEvent pingEvent ? new Component[]{pingEvent.motd()} : new Component[0];
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.isDefault) {
         return null;
      } else if (this.getParser().getHasDelayBefore().isTrue()) {
         Skript.error("'" + this.toString(null, false) + "' cannot be changed after the event has passed");
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
      if (event instanceof ServerListPingEvent pingEvent) {
         pingEvent.motd((Component)(switch (mode) {
            case SET -> (Component)delta[0];
            case DELETE -> Component.empty();
            case RESET -> Bukkit.motd();
            default -> throw new IllegalArgumentException();
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
      return "the " + (this.isDefault ? "default " : "") + "MOTD";
   }
}
