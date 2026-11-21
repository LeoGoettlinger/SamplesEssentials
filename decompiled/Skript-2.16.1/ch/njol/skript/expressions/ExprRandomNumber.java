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
import ch.njol.util.Math2;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Random Numbers")
@Description({"A given amount of random numbers or integers between two given numbers. Use 'number' if you want any number with decimal parts, or use use 'integer' if you only want whole numbers.", "Please note that the order of the numbers doesn't matter, i.e. <code>random number between 2 and 1</code> will work as well as <code>random number between 1 and 2</code>."})
@Example.Examples({@Example("set the player's health to a random number between 5 and 10"), @Example("send \"You rolled a %random integer from 1 to 6%!\" to the player"), @Example("set {_chances::*} to 5 random integers between 5 and 96"), @Example("set {_decimals::*} to 3 random numbers between 2.7 and -1.5")})
@Since({"1.4, 2.10 (Multiple random numbers)"})
public class ExprRandomNumber extends SimpleExpression<Number> {
   @Nullable
   private Expression<Integer> amount;
   private Expression<Number> lower;
   private Expression<Number> upper;
   private boolean isInteger;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.amount = (Expression<Integer>)exprs[0];
      this.lower = (Expression<Number>)exprs[1];
      this.upper = (Expression<Number>)exprs[2];
      this.isInteger = parseResult.hasTag("integer");
      return true;
   }

   @Nullable
   protected Number[] get(Event event) {
      Number lowerNumber = this.lower.getSingle(event);
      Number upperNumber = this.upper.getSingle(event);
      if (upperNumber != null && lowerNumber != null && Double.isFinite(lowerNumber.doubleValue()) && Double.isFinite(upperNumber.doubleValue())) {
         Integer amount = this.amount == null ? 1 : this.amount.getSingle(event);
         if (amount != null && amount > 0) {
            double lower = Math.min(lowerNumber.doubleValue(), upperNumber.doubleValue());
            double upper = Math.max(lowerNumber.doubleValue(), upperNumber.doubleValue());
            Random random = ThreadLocalRandom.current();
            if (this.isInteger) {
               Long[] longs = new Long[amount];
               long floored_upper = Math2.floor(upper);
               long ceiled_lower = Math2.ceil(lower);
               if (!(upper - lower < 1.0) || ceiled_lower - floored_upper > 1L) {
                  for (int i = 0; i < amount; i++) {
                     longs[i] = Math2.ceil(lower) + Math2.mod(random.nextLong(), floored_upper - ceiled_lower + 1L);
                  }

                  return longs;
               } else if (floored_upper == ceiled_lower || lower == ceiled_lower) {
                  Arrays.fill(longs, Long.valueOf(ceiled_lower));
                  return longs;
               } else if (upper == floored_upper) {
                  Arrays.fill(longs, Long.valueOf(floored_upper));
                  return longs;
               } else {
                  return new Long[0];
               }
            } else {
               Double[] doubles = new Double[amount];

               for (int i = 0; i < amount; i++) {
                  doubles[i] = Math.min(lower + random.nextDouble() * (upper - lower), upper);
               }

               return doubles;
            }
         } else {
            return new Number[0];
         }
      } else {
         return new Number[0];
      }
   }

   @Override
   public boolean isSingle() {
      return this.amount instanceof Literal ? ((Literal)this.amount).getSingle() == 1 : this.amount == null;
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return this.isInteger ? Long.class : Double.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.amount == null ? "a" : this.amount.toString(event, debug))
         + " random "
         + (this.isInteger ? "integer" : "number")
         + (this.amount == null ? "" : "s")
         + " between "
         + this.lower.toString(event, debug)
         + " and "
         + this.upper.toString(event, debug);
   }

   static {
      Skript.registerExpression(
         ExprRandomNumber.class, Number.class, ExpressionType.COMBINED, "[a|%-integer%] random (:integer|number)[s] (from|between) %number% (to|and) %number%"
      );
   }
}
