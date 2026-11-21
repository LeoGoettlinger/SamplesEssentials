package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Join & Split")
@Description({"Joins several texts with a common delimiter (e.g. \", \"), or splits a text into multiple texts at a given delimiter."})
@Example.Examples({@Example("message \"Online players: %join all players' names with \"\" | \"\"%\" # %all players% would use the default \"x, y, and z\""), @Example("set {_s::*} to the string argument split at \",\"")})
@Since({"2.1, 2.5.2 (regex support), 2.7 (case sensitivity), 2.10 (without trailing string)"})
public class ExprJoinSplit extends SimpleExpression<String> {
   private boolean join;
   private boolean regex;
   private boolean caseSensitivity;
   private boolean removeTrailing;
   private Expression<String> strings;
   @Nullable
   private Expression<String> delimiter;
   @Nullable
   private Pattern pattern;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.join = matchedPattern == 0;
      this.regex = matchedPattern >= 3;
      this.caseSensitivity = SkriptConfig.caseSensitive.value() || parseResult.hasTag("case");
      this.removeTrailing = parseResult.hasTag("trailing");
      this.strings = (Expression<String>)exprs[0];
      this.delimiter = (Expression<String>)exprs[1];
      if (!this.join && this.delimiter instanceof Literal<String> literal) {
         String stringPattern = literal.getSingle();

         try {
            this.pattern = this.compilePattern(stringPattern);
         } catch (PatternSyntaxException var8) {
            Skript.error("'" + stringPattern + "' is not a valid regular expression");
            return false;
         }
      }

      return true;
   }

   @Nullable
   protected String[] get(Event event) {
      String[] strings = this.strings.getArray(event);
      String delimiter = this.delimiter != null ? this.delimiter.getSingle(event) : "";
      if (strings.length == 0 || delimiter == null) {
         return new String[0];
      } else if (this.join) {
         return new String[]{StringUtils.join(strings, delimiter)};
      } else {
         try {
            Pattern pattern = this.pattern;
            if (pattern == null) {
               pattern = this.compilePattern(delimiter);
            }

            return pattern.split(strings[0], this.removeTrailing ? 0 : -1);
         } catch (PatternSyntaxException var5) {
            return new String[0];
         }
      }
   }

   @Override
   public boolean isSingle() {
      return this.join;
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public Expression<? extends String> simplify() {
      return (Expression<? extends String>)(!(this.strings instanceof Literal) || this.delimiter != null && !(this.delimiter instanceof Literal)
         ? this
         : SimplifiedLiteral.fromExpression(this));
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      if (this.join) {
         builder.append("join", this.strings);
         if (this.delimiter != null) {
            builder.append("with", this.delimiter);
         }

         return builder.toString();
      } else {
         assert this.delimiter != null;

         if (this.regex) {
            builder.append("regex");
         }

         builder.append("split", this.strings, "at", this.delimiter);
         if (this.removeTrailing) {
            builder.append("without trailing text");
         }

         if (!this.regex) {
            builder.append("(case sensitive:", this.caseSensitivity + ")");
         }

         return builder.toString();
      }
   }

   private Pattern compilePattern(String delimiter) {
      return Pattern.compile(this.regex ? delimiter : (this.caseSensitivity ? "" : "(?i)") + Pattern.quote(delimiter));
   }

   static {
      Skript.registerExpression(
         ExprJoinSplit.class,
         String.class,
         ExpressionType.COMBINED,
         "(concat[enate]|join) %strings% [(with|using|by) [[the] delimiter] %-string%]",
         "split %string% (at|using|by) [[the] delimiter] %string% [case:with case sensitivity] [trailing:without [the] trailing [empty] (string|text)]",
         "%string% split (at|using|by) [[the] delimiter] %string% [case:with case sensitivity] [trailing:without [the] trailing [empty] (string|text)]",
         "regex split %string% (at|using|by) [[the] delimiter] %string% [trailing:without [the] trailing [empty] (string|text)]",
         "regex %string% split (at|using|by) [[the] delimiter] %string% [trailing:without [the] trailing [empty] (string|text)]"
      );
   }
}
