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
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Alphanumeric")
@Description({"Checks if the given string is alphanumeric."})
@Example("if the argument is not alphanumeric:\n\tsend \"Invalid name!\"\n")
@Since({"2.4"})
public class CondAlphanumeric extends Condition {
   private Expression<String> strings;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.strings = (Expression<String>)exprs[0];
      this.setNegated(matchedPattern == 1);
      return true;
   }

   @Override
   public boolean check(Event e) {
      return this.isNegated() ^ this.strings.check(e, StringUtils::isAlphanumeric);
   }

   @Override
   public Condition simplify() {
      return (Condition)(this.strings instanceof Literal ? SimplifiedCondition.fromCondition(this) : this);
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return this.strings.toString(e, debug) + " is" + (this.isNegated() ? "n't" : "") + " alphanumeric";
   }

   static {
      Skript.registerCondition(CondAlphanumeric.class, "%strings% (is|are) alphanumeric", "%strings% (isn't|is not|aren't|are not) alphanumeric");
   }
}
