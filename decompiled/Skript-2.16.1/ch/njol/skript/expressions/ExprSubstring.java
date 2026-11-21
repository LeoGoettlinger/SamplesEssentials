package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Substring")
@Description({"Extracts part of a text. You can either get the first &lt;x&gt; characters, the last &lt;x&gt; characters, the character at index &lt;x&gt;, or the characters between indices &lt;x&gt; and &lt;y&gt;. The indices &lt;x&gt; and &lt;y&gt; should be between 1 and the <a href='#ExprLength'>length</a> of the text (other values will be fit into this range)."})
@Keywords({"substring", "subtext"})
@Example.Examples({@Example("set {_s} to the first 5 characters of the text argument"), @Example("message \"%subtext of {_s} from characters 2 to (the length of {_s} - 1)%\" # removes the first and last character from {_s} and sends it to the player or console"), @Example("set {_characters::*} to characters at 1, 2 and 7 in player's display name"), @Example("send the last character of all players' names")})
@Since({"2.1, 2.5.2 (character at, multiple strings support)"})
public class ExprSubstring extends SimpleExpression<String> {
   private Expression<String> string;
   @Nullable
   private Expression<Number> start;
   @Nullable
   private Expression<Number> end;
   private boolean usedSubstring;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.usedSubstring = matchedPattern == 0;
      this.string = (Expression<String>)exprs[this.usedSubstring ? 0 : 1];
      this.start = (Expression<Number>)(this.usedSubstring
         ? exprs[1]
         : (parseResult.mark == 1 ? null : (exprs[0] == null ? new SimpleLiteral<>(1, false) : exprs[0])));
      this.end = (Expression<Number>)(this.usedSubstring
         ? exprs[2]
         : (parseResult.mark == 2 ? null : (exprs[0] == null ? new SimpleLiteral<>(1, false) : exprs[0])));

      assert this.end != null || this.start != null;

      return true;
   }

   @Nullable
   protected String[] get(Event e) {
      List<String> parts = new ArrayList<>();
      String[] strings = this.string.getArray(e);
      if (strings == null) {
         return new String[0];
      } else {
         for (String string : strings) {
            if (this.start != null && !this.start.isSingle()) {
               Number[] i = this.start.getArray(e);
               if (i == null) {
                  return new String[0];
               }

               for (Number p : i) {
                  if (p.intValue() <= string.length() && p.intValue() >= 1) {
                     parts.add(string.substring(p.intValue() - 1, p.intValue()));
                  }
               }
            } else {
               Number d1 = (Number)(this.start != null ? this.start.getSingle(e) : 1);
               Number d2 = (Number)(this.end != null ? this.end.getSingle(e) : string.length());
               if (d1 != null && d2 != null) {
                  if (this.end == null) {
                     d1 = string.length() - d1.intValue() + 1;
                  }

                  int i1 = Math.max(d1.intValue() - 1, 0);
                  int i2 = Math.min(d2.intValue(), string.length());
                  if (i1 < i2) {
                     parts.add(string.substring(i1, i2));
                  }
               }
            }
         }

         return parts.toArray(new String[parts.size()]);
      }
   }

   @Override
   public boolean isSingle() {
      return this.string.isSingle() && (this.start == null || this.start.isSingle());
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public Expression<? extends String> simplify() {
      return (Expression<? extends String>)(!(this.string instanceof Literal)
            || this.start != null && !(this.start instanceof Literal)
            || this.end != null && !(this.end instanceof Literal)
         ? this
         : SimplifiedLiteral.fromExpression(this));
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      if (this.start == null) {
         assert this.end != null;

         return "the first " + this.end.toString(e, debug) + " characters of " + this.string.toString(e, debug);
      } else if (this.end == null) {
         assert this.start != null;

         return "the last " + this.start.toString(e, debug) + " characters of " + this.string.toString(e, debug);
      } else {
         return this.usedSubstring
            ? "the substring of " + this.string.toString(e, debug) + " from index " + this.start.toString(e, debug) + " to " + this.end.toString(e, debug)
            : "the character at " + (this.start.isSingle() ? "index " : "indexes ") + this.start.toString(e, debug) + " in " + this.string.toString(e, debug);
      }
   }

   static {
      Skript.registerExpression(
         ExprSubstring.class,
         String.class,
         ExpressionType.COMBINED,
         "[the] (part|sub[ ](text|string)) of %strings% (between|from) (ind(ex|ices)|character[s]|) %number% (and|to) (index|character|) %number%",
         "[the] (1¦first|2¦last) [%-number%] character[s] of %strings%",
         "[the] %number% (1¦first|2¦last) characters of %strings%",
         "[the] character[s] at [(index|position|indexes|indices|positions)] %numbers% (in|of) %strings%"
      );
   }
}
