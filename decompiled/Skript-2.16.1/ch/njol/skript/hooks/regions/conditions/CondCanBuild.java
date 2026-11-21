package ch.njol.skript.hooks.regions.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Can Build")
@Description({"Tests whether a player is allowed to build at a certain location.", "This condition requires a supported <a href='#region'>regions</a> plugin to be installed."})
@Example("command /setblock <material>:\n\tdescription: set the block at your crosshair to a different type\n\ttrigger:\n\t\tplayer cannot build at the targeted block:\n\t\t\tmessage \"You do not have permission to change blocks there!\"\n\t\t\tstop\n\t\tset the targeted block to argument\n")
@Since({"2.0"})
@RequiredPlugins({"Supported regions plugin"})
public class CondCanBuild extends Condition {
   private Expression<Player> players;
   Expression<Location> locations;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.players = (Expression<Player>)exprs[0];
      this.locations = Direction.combine((Expression<? extends Direction>)exprs[1], (Expression<? extends Location>)exprs[2]);
      this.setNegated(matchedPattern == 1);
      ScriptWarning.printDeprecationWarning(
         "Skript's region syntaxes are deprecated and will be removed in a future release. For WorldGuard support, we recommend using skript-worldguard: https://github.com/SkriptLang/skript-worldguard"
      );
      return true;
   }

   @Override
   public boolean check(Event event) {
      return this.players.check(event, player -> this.locations.check(event, location -> RegionsPlugin.canBuild(player, location), this.isNegated()));
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return this.players.toString(e, debug) + " can build " + this.locations.toString(e, debug);
   }

   static {
      Skript.registerCondition(
         CondCanBuild.class,
         "%players% (can|(is|are) allowed to) build %directions% %locations%",
         "%players% (can('t|not)|(is|are)(n't| not) allowed to) build %directions% %locations%"
      );
   }
}
