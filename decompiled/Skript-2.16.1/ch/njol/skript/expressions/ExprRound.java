package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Rounding")
@Description({"Rounds numbers normally, up (ceiling) or down (floor) respectively."})
@Example.Examples({@Example("set {var} to rounded health of player"), @Example("set line 1 of the block to \"%rounded (1.5 * player's level)%\""), @Example("add rounded down argument to the player's health")})
@Since({"2.0"})
public class ExprRound extends PropertyExpression<Number, Long> {
   private static final Patterns<ExprRound.RoundType> patterns = new Patterns<>(
      new Object[][]{
         {"[a|the] (round[ed] down|floored) %numbers%", ExprRound.RoundType.FLOOR},
         {"%numbers% (round[ed] down|floored)", ExprRound.RoundType.FLOOR},
         {"[a|the] round[ed] %numbers%", ExprRound.RoundType.ROUND},
         {"%numbers% round[ed]", ExprRound.RoundType.ROUND},
         {"[a|the] (round[ed] up|ceil[ing]ed) %numbers%", ExprRound.RoundType.CEIL},
         {"%numbers% (round[ed] up|ceil[ing]ed)", ExprRound.RoundType.CEIL}
      }
   );
   private ExprRound.RoundType roundType;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends Number>)exprs[0]);
      this.roundType = patterns.getInfo(matchedPattern);
      return true;
   }

   @Nullable
   protected Long[] get(Event event, Number[] source) {
      return this.get(source, number -> {
         if (number instanceof Integer integer) {
            return integer.longValue();
         } else if (number instanceof Long long1) {
            return long1;
         } else {
            return switch (this.roundType) {
               case FLOOR -> Math2.floor(number.doubleValue());
               case ROUND -> Math2.round(number.doubleValue());
               case CEIL -> Math2.ceil(number.doubleValue());
            };
         }
      });
   }

   @Override
   public Class<? extends Long> getReturnType() {
      return Long.class;
   }

   @Override
   public Expression<? extends Long> simplify() {
      return (Expression<? extends Long>)(this.getExpr() instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

      builder.append(switch (this.roundType) {
         case FLOOR -> "floored";
         case ROUND -> "rounded";
         case CEIL -> "ceiled";
      });
      builder.append(this.getExpr());
      return builder.toString();
   }

   static {
      Skript.registerExpression(ExprRound.class, Long.class, ExpressionType.PROPERTY, patterns.getPatterns());
   }

   private static enum RoundType {
      FLOOR,
      ROUND,
      CEIL;
   }
}
