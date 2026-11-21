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

@Name("Feed")
@Description({"Feeds the specified players."})
@Example.Examples({@Example("feed all players"), @Example("feed the player by 5 beefs")})
@Since({"2.2-dev34"})
public class EffFeed extends Effect {
   private Expression<Player> players;
   @Nullable
   private Expression<Number> beefs;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.players = (Expression<Player>)exprs[0];
      this.beefs = (Expression<Number>)exprs[1];
      return true;
   }

   @Override
   protected void execute(Event e) {
      int level = 20;
      if (this.beefs != null) {
         Number n = this.beefs.getSingle(e);
         if (n == null) {
            return;
         }

         level = n.intValue();
      }

      for (Player player : this.players.getArray(e)) {
         player.setFoodLevel(player.getFoodLevel() + level);
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "feed " + this.players.toString(e, debug) + (this.beefs != null ? " by " + this.beefs.toString(e, debug) : "");
   }

   static {
      Skript.registerEffect(EffFeed.class, "feed [the] %players% [by %-number% [beef[s]]]");
   }
}
