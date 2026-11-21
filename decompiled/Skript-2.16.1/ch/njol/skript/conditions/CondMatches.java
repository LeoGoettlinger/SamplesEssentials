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
import java.util.Arrays;
import java.util.regex.Pattern;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Matches")
@Description({"Checks whether the defined strings match the input regexes (Regular expressions)."})
@Example("on chat:\n\tif message partially matches \"\\d\":\n\t\tsend \"Message contains a digit!\"\n\tif message doesn't match \"[A-Za-z]+\":\n\t\tsend \"Message doesn't only contain letters!\"\n")
@Since({"2.5.2"})
public class CondMatches extends Condition {
   Expression<String> strings;
   Expression<String> regex;
   boolean partial;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.strings = (Expression<String>)exprs[0];
      this.regex = (Expression<String>)exprs[1];
      this.partial = matchedPattern == 1;
      this.setNegated(parseResult.mark == 1);
      return true;
   }

   @Override
   public boolean check(Event e) {
      String[] txt = this.strings.getAll(e);
      String[] regexes = this.regex.getAll(e);
      if (txt.length >= 1 && regexes.length >= 1) {
         boolean stringAnd = this.strings.getAnd();
         boolean regexAnd = this.regex.getAnd();
         boolean result;
         if (stringAnd) {
            if (regexAnd) {
               result = Arrays.stream(txt)
                  .allMatch(str -> Arrays.stream(regexes).parallel().map(Pattern::compile).allMatch(pattern -> this.matches(str, pattern)));
            } else {
               result = Arrays.stream(txt)
                  .allMatch(str -> Arrays.stream(regexes).parallel().map(Pattern::compile).anyMatch(pattern -> this.matches(str, pattern)));
            }
         } else if (regexAnd) {
            result = Arrays.stream(txt)
               .anyMatch(str -> Arrays.stream(regexes).parallel().map(Pattern::compile).allMatch(pattern -> this.matches(str, pattern)));
         } else {
            result = Arrays.stream(txt)
               .anyMatch(str -> Arrays.stream(regexes).parallel().map(Pattern::compile).anyMatch(pattern -> this.matches(str, pattern)));
         }

         return result == this.isNegated();
      } else {
         return false;
      }
   }

   public boolean matches(String str, Pattern pattern) {
      return this.partial ? pattern.matcher(str).find() : str.matches(pattern.pattern());
   }

   @Override
   public Condition simplify() {
      return (Condition)(this.strings instanceof Literal && this.regex instanceof Literal ? SimplifiedCondition.fromCondition(this) : this);
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return this.strings.toString(e, debug) + " " + (this.isNegated() ? "doesn't match" : "matches") + " " + this.regex.toString(e, debug);
   }

   static {
      Skript.registerCondition(
         CondMatches.class,
         "%strings% (1¦match[es]|2¦do[es](n't| not) match) %strings%",
         "%strings% (1¦partially match[es]|2¦do[es](n't| not) partially match) %strings%"
      );
   }
}
