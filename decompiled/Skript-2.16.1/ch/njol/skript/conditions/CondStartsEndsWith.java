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

@Name("Starts/Ends With")
@Description({"Checks if a text starts or ends with another."})
@Example("if the argument starts with \"test\" or \"debug\":\n\tsend \"Stop!\"\n")
@Since({"2.2-dev36, 2.5.1 (multiple strings support)"})
public class CondStartsEndsWith extends Condition {
   private Expression<String> strings;
   private Expression<String> affix;
   private boolean usingEnds;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.strings = (Expression<String>)exprs[0];
      this.affix = (Expression<String>)exprs[1];
      this.usingEnds = parseResult.mark == 1;
      this.setNegated(matchedPattern == 1);
      return true;
   }

   @Override
   public boolean check(Event e) {
      String[] affixes = this.affix.getAll(e);
      return affixes.length < 1 ? false : this.strings.check(e, string -> {
         if (this.usingEnds) {
            if (this.affix.getAnd()) {
               for (String str : affixes) {
                  if (!string.endsWith(str)) {
                     return false;
                  }
               }

               return true;
            }

            for (String strx : affixes) {
               if (string.endsWith(strx)) {
                  return true;
               }
            }
         } else {
            if (this.affix.getAnd()) {
               for (String strxx : affixes) {
                  if (!string.startsWith(strxx)) {
                     return false;
                  }
               }

               return true;
            }

            for (String strxxx : affixes) {
               if (string.startsWith(strxxx)) {
                  return true;
               }
            }
         }

         return false;
      }, this.isNegated());
   }

   @Override
   public Condition simplify() {
      return (Condition)(this.strings instanceof Literal && this.affix instanceof Literal ? SimplifiedCondition.fromCondition(this) : this);
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return this.isNegated()
         ? this.strings.toString(e, debug) + " doesn't " + (this.usingEnds ? "end" : "start") + " with " + this.affix.toString(e, debug)
         : this.strings.toString(e, debug) + (this.usingEnds ? " ends" : " starts") + " with " + this.affix.toString(e, debug);
   }

   static {
      Skript.registerCondition(
         CondStartsEndsWith.class, "%strings% (start|1¦end)[s] with %strings%", "%strings% (doesn't|does not|do not|don't) (start|1¦end) with %strings%"
      );
   }
}
