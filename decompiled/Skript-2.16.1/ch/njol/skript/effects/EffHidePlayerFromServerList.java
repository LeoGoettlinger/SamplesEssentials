package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.Nullable;

@Name("Hide Player from Server List")
@Description({"Hides a player from the <a href='#ExprHoverList'>hover list</a> and decreases the <a href='#ExprOnlinePlayersCount'>online players count</a> (only if the player count wasn't changed before)."})
@Example("on server list ping:\n\thide {vanished::*} from the server list\n")
@Since({"2.3"})
public class EffHidePlayerFromServerList extends Effect implements EventRestrictedSyntax {
   private Expression<Player> players;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (isDelayed == Kleenean.TRUE) {
         Skript.error("Can't hide players from the server list anymore after the server list ping event has already passed");
         return false;
      } else {
         this.players = (Expression<Player>)exprs[0];
         return true;
      }
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(ServerListPingEvent.class, PaperServerListPingEvent.class);
   }

   @Override
   protected void execute(Event e) {
      if (e instanceof ServerListPingEvent) {
         Iterator<Player> it = ((ServerListPingEvent)e).iterator();
         Iterators.removeAll(it, Arrays.asList(this.players.getArray(e)));
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "hide " + this.players.toString(e, debug) + " from the server list";
   }

   static {
      Skript.registerEffect(
         EffHidePlayerFromServerList.class, "hide %players% (in|on|from) [the] server list", "hide %players%'[s] info[rmation] (in|on|from) [the] server list"
      );
   }
}
