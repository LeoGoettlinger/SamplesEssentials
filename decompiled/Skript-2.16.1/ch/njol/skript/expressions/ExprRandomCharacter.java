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
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Random Character")
@Description({"One or more random characters between two given characters. Use 'alphanumeric' if you want only alphanumeric characters.", "This expression uses the Unicode numerical code of a character to determine which characters are between the two given characters.", "If strings of more than one character are given, only the first character of each is used."})
@Example.Examples({@Example("set {_captcha} to join (5 random characters between \"a\" and \"z\") with \"\""), @Example("send 3 random alphanumeric characters between \"0\" and \"z\"")})
@Since({"2.8.0"})
public class ExprRandomCharacter extends SimpleExpression<String> {
   @Nullable
   private Expression<Integer> amount;
   private Expression<String> from;
   private Expression<String> to;
   private boolean isAlphanumeric;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.amount = (Expression<Integer>)exprs[0];
      this.from = (Expression<String>)exprs[1];
      this.to = (Expression<String>)exprs[2];
      this.isAlphanumeric = parseResult.hasTag("alphanumeric");
      return true;
   }

   @Nullable
   protected String[] get(Event event) {
      Integer amount = this.amount == null ? 1 : this.amount.getSingle(event);
      if (amount != null && amount > 0) {
         String from = this.from.getSingle(event);
         String to = this.to.getSingle(event);
         if (from != null && to != null) {
            if (from.length() >= 1 && to.length() >= 1) {
               Random random = ThreadLocalRandom.current();
               char fromChar = from.charAt(0);
               char toChar = to.charAt(0);
               int min = Math.min((int)fromChar, (int)toChar);
               int max = Math.max((int)fromChar, (int)toChar);
               String[] chars = new String[amount];
               if (this.isAlphanumeric) {
                  StringBuilder validChars = new StringBuilder();

                  for (int c = min; c <= max; c++) {
                     if (Character.isLetterOrDigit(c)) {
                        validChars.append((char)c);
                     }
                  }

                  if (validChars.length() == 0) {
                     return new String[0];
                  } else {
                     for (int i = 0; i < amount; i++) {
                        chars[i] = String.valueOf(validChars.charAt(random.nextInt(validChars.length())));
                     }

                     return chars;
                  }
               } else {
                  for (int i = 0; i < amount; i++) {
                     chars[i] = String.valueOf((char)(random.nextInt(max - min + 1) + min));
                  }

                  return chars;
               }
            } else {
               return new String[0];
            }
         } else {
            return new String[0];
         }
      } else {
         return new String[0];
      }
   }

   @Override
   public boolean isSingle() {
      return this.amount instanceof Literal ? ((Literal)this.amount).getSingle() == 1 : this.amount == null;
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.amount != null ? this.amount.toString(event, debug) : "a")
         + " random character between "
         + this.from.toString(event, debug)
         + " and "
         + this.to.toString(event, debug);
   }

   static {
      Skript.registerExpression(
         ExprRandomCharacter.class,
         String.class,
         ExpressionType.COMBINED,
         "[a|%-integer%] random [:alphanumeric] character[s] (from|between) %string% (to|and) %string%"
      );
   }
}
