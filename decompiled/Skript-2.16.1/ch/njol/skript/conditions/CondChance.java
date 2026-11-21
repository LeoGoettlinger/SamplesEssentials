package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Chance")
@Description({"A condition that randomly succeeds or fails.\nValid values are between 0% and 100%, or if the percent sign is omitted, between 0 and 1.\n"})
@Example.Examples({@Example("chance of 50%:\n\tdrop a diamond at location(100, 100, 100, \"world')\n"), @Example("chance of {chance}% # {chance} between 0 and 100"), @Example("chance of {chance} # {chance} between 0 and 1"), @Example("if chance of 99% fails:\n\tbroadcast \"Haha loser! *points and laughs*\"\n")})
@Since({"1.0, 2.14 (chance fails)"})
public class CondChance extends Condition {
   private Expression<Number> chance;
   private boolean percent;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.chance = (Expression<Number>)exprs[0];
      this.percent = parseResult.mark == 1;
      this.setNegated(parseResult.hasTag("fail"));
      return true;
   }

   @Override
   public boolean check(Event event) {
      Number chance = this.chance.getSingle(event);
      if (chance == null) {
         return false;
      } else {
         boolean result = Math.random() < (this.percent ? chance.doubleValue() / 100.0 : chance.doubleValue());
         return result == !this.isNegated();
      }
   }

   @Override
   public Condition simplify() {
      if (this.chance instanceof Literal<Number> litChance) {
         Number chance = litChance.getSingle();
         double maxValue = this.percent ? 100.0 : 1.0;
         if (chance.doubleValue() >= maxValue || chance.doubleValue() <= 0.0) {
            return SimplifiedCondition.fromCondition(this);
         }
      }

      return this;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      String baseString = "chance of " + this.chance.toString(event, debug) + (this.percent ? "%" : "");
      if (this.isNegated()) {
         baseString = baseString + " failed";
      }

      return baseString;
   }

   static {
      Skript.registerCondition(CondChance.class, "chance of %number%(1:\\%|) [fail:(fails|failed)]");
   }
}
