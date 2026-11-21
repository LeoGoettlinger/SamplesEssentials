package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.Nullable;

@Name("Max Players")
@Description({"The count of max players. This can be changed in a <a href='#server_list_ping'>server list ping</a> event only.", "'real max players' returns the real count of max players of the server and can be modified on Paper 1.16 or later."})
@Example("on server list ping:\n\tset the max players count to (online players count + 1)\n")
@Since({"2.3, 2.7 (modify max real players)"})
public class ExprMaxPlayers extends SimpleExpression<Integer> {
   private boolean isReal;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      boolean isServerPingEvent = this.getParser().isCurrentEvent(ServerListPingEvent.class) || this.getParser().isCurrentEvent(PaperServerListPingEvent.class);
      if (parseResult.mark == 2 && !isServerPingEvent) {
         Skript.error("The 'shown' max players count expression can't be used outside of a server list ping event");
         return false;
      } else {
         this.isReal = parseResult.mark == 0 && !isServerPingEvent || parseResult.mark == 1;
         return true;
      }
   }

   @Nullable
   public Integer[] get(Event event) {
      if (!this.isReal && !(event instanceof ServerListPingEvent)) {
         return null;
      } else {
         return this.isReal ? CollectionUtils.array(Bukkit.getMaxPlayers()) : CollectionUtils.array(((ServerListPingEvent)event).getMaxPlayers());
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!this.isReal && this.getParser().getHasDelayBefore().isTrue()) {
         Skript.error("Can't change the fake max players count anymore after the server list ping event has already passed");
         return null;
      } else {
         switch (mode) {
            case SET:
            case ADD:
            case REMOVE:
               return CollectionUtils.array(Number.class);
            case RESET:
            case DELETE:
               if (!this.isReal) {
                  return CollectionUtils.array(Number.class);
               }
            default:
               return null;
         }
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int amount = delta == null ? 0 : ((Number)delta[0]).intValue();
      if (this.isReal || event instanceof ServerListPingEvent) {
         if (this.isReal) {
            switch (mode) {
               case SET:
                  Bukkit.setMaxPlayers(amount);
                  break;
               case ADD:
                  Bukkit.setMaxPlayers(Bukkit.getMaxPlayers() + amount);
                  break;
               case REMOVE:
                  Bukkit.setMaxPlayers(Bukkit.getMaxPlayers() - amount);
            }
         } else {
            ServerListPingEvent pingEvent = (ServerListPingEvent)event;
            switch (mode) {
               case SET:
                  pingEvent.setMaxPlayers(amount);
                  break;
               case ADD:
                  pingEvent.setMaxPlayers(pingEvent.getMaxPlayers() + amount);
                  break;
               case REMOVE:
                  pingEvent.setMaxPlayers(pingEvent.getMaxPlayers() - amount);
                  break;
               case RESET:
               case DELETE:
                  pingEvent.setMaxPlayers(Bukkit.getMaxPlayers());
            }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the count of " + (this.isReal ? "real max players" : "max players");
   }

   static {
      Skript.registerExpression(
         ExprMaxPlayers.class,
         Integer.class,
         ExpressionType.PROPERTY,
         "[the] [1:(real|default)|2:(fake|shown|displayed)] max[imum] player[s] [count|amount|number|size]",
         "[the] [1:(real|default)|2:(fake|shown|displayed)] max[imum] (count|amount|number|size) of players"
      );
   }
}
