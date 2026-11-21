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

@Name("Make Fly")
@Description({"Forces a player to start/stop flying."})
@Example.Examples({@Example("make player fly"), @Example("force all players to stop flying")})
@Since({"2.2-dev34"})
public class EffMakeFly extends Effect {
   private Expression<Player> players;
   private boolean flying;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.players = (Expression<Player>)exprs[0];
      this.flying = parseResult.mark != 1;
      return true;
   }

   @Override
   protected void execute(Event e) {
      for (Player player : this.players.getArray(e)) {
         player.setAllowFlight(this.flying);
         player.setFlying(this.flying);
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "make " + this.players.toString(e, debug) + (this.flying ? " start " : " stop ") + "flying";
   }

   static {
      if (Skript.methodExists(Player.class, "setFlying", boolean.class)) {
         Skript.registerEffect(EffMakeFly.class, "force %players% to [(start|1¦stop)] fly[ing]", "make %players% (start|1¦stop) flying", "make %players% fly");
      }
   }
}
