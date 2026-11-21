package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Toggle Flight")
@Description({"Toggle the <a href='#ExprFlightMode'>flight mode</a> of a player."})
@Example("allow flight to event-player")
@Since({"2.3"})
public class EffToggleFlight extends Effect {
   private Expression<Player> players;
   private boolean allow;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.players = (Expression<Player>)exprs[0];
      this.allow = matchedPattern == 0;
      return true;
   }

   @Override
   protected void execute(Event e) {
      for (Player player : this.players.getArray(e)) {
         player.setAllowFlight(this.allow);
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "allow flight to " + this.players.toString(e, debug);
   }

   static {
      Skript.registerEffect(EffToggleFlight.class, "(allow|enable) (fly|flight) (for|to) %players%", "(disallow|disable) (fly|flight) (for|to) %players%");
   }
}
