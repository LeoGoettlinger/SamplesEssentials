package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Characters Between")
@Description({"All characters between two given characters, useful for generating random strings. This expression uses the Unicode numerical code of a character to determine which characters are between the two given characters. The <a href=\"https://www.asciitable.com/\">ASCII table linked here</a> shows this ordering for the first 256 characters.", "If you would like only alphanumeric characters you can use the 'alphanumeric' option in the expression.", "If strings of more than one character are given, only the first character of each is used."})
@Example.Examples({@Example("loop characters from \"a\" to \"f\":\n\tbroadcast \"%loop-value%\"\n"), @Example("# 0123456789:;<=>?@ABC... ...uvwxyz\nsend characters between \"0\" and \"z\"\n"), @Example("# 0123456789ABC... ...uvwxyz\nsend alphanumeric characters between \"0\" and \"z\"\n")})
@Since({"2.8.0"})
public class ExprCharacters extends SimpleExpression<String> {
   private Expression<String> start;
   private Expression<String> end;
   private boolean isAlphanumeric;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.start = (Expression<String>)exprs[0];
      this.end = (Expression<String>)exprs[1];
      this.isAlphanumeric = parseResult.hasTag("alphanumeric");
      return true;
   }

   @Nullable
   protected String[] get(Event event) {
      String start = this.start.getSingle(event);
      String end = this.end.getSingle(event);
      if (start != null && end != null) {
         if (start.length() >= 1 && end.length() >= 1) {
            char startChar = start.charAt(0);
            char endChar = end.charAt(0);
            boolean reversed = startChar > endChar;
            char delta = (char)(reversed ? '\uffff' : 1);
            int min = Math.min((int)startChar, (int)endChar);
            int max = Math.max((int)startChar, (int)endChar);
            String[] chars = new String[max - min + 1];

            for (char c = startChar; min <= c && c <= max; c = (char)(c + delta)) {
               if (!this.isAlphanumeric || Character.isLetterOrDigit(c)) {
                  chars[c - min] = String.valueOf(c);
               }
            }

            if (reversed) {
               ArrayUtils.reverse(chars);
            }

            return chars;
         } else {
            return new String[0];
         }
      } else {
         return new String[0];
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public Expression<? extends String> simplify() {
      return (Expression<? extends String>)(this.start instanceof Literal && this.end instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "all the "
         + (this.isAlphanumeric ? "alphanumeric " : "")
         + "characters between "
         + this.start.toString(event, debug)
         + " and "
         + this.end.toString(event, debug);
   }

   static {
      Skript.registerExpression(
         ExprCharacters.class,
         String.class,
         ExpressionType.COMBINED,
         "[(all [[of] the]|the)] [:alphanumeric] characters (between|from) %string% (and|to) %string%"
      );
   }
}
