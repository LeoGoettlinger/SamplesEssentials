package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("PvP")
@Description({"Checks the PvP state of a world."})
@Example.Examples({@Example("PvP is enabled"), @Example("PvP is disabled in \"world\"")})
@Since({"1.3.4"})
public class CondPvP extends Condition {
   private static final boolean PVP_GAME_RULE_EXISTS = Skript.fieldExists(GameRule.class, "PVP");
   private Expression<World> worlds;
   private boolean enabled;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.worlds = (Expression<World>)exprs[0];
      this.enabled = matchedPattern == 0;
      return true;
   }

   @Override
   public boolean check(Event event) {
      return PVP_GAME_RULE_EXISTS
         ? this.worlds.check(event, world -> (Boolean)world.getGameRuleValue(GameRule.PVP) == this.enabled, this.isNegated())
         : this.worlds.check(event, world -> world.getPVP() == this.enabled, this.isNegated());
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "PvP is " + (this.enabled ? "enabled" : "disabled") + " in " + this.worlds.toString(event, debug);
   }

   static {
      Skript.registerCondition(CondPvP.class, "(is PvP|PvP is) enabled [in %worlds%]", "(is PvP|PvP is) disabled [in %worlds%]");
   }
}
