package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

@Name("Timespan Details")
@Description({"Retrieve specific information of a <a href=\"/#timespan\">timespan</a> such as hours/minutes/etc."})
@Example.Examples({@Example("set {_t} to difference between now and {Payouts::players::%uuid of player%::last-date}"), @Example("send \"It has been %days of {_t}% day(s) since last payout.\"")})
@Since({"2.9.0"})
public class ExprTimespanDetails extends SimplePropertyExpression<Timespan, Long> {
   private Timespan.TimePeriod type;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.type = Timespan.TimePeriod.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Long convert(Timespan time) {
      return time.getAs(Timespan.TimePeriod.MILLISECOND) / this.type.getTime();
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
   protected String getPropertyName() {
      return this.type.name().toLowerCase(Locale.ENGLISH);
   }

   static {
      register(ExprTimespanDetails.class, Long.class, "(:(tick|second|minute|hour|day|week|month|year))s", "timespans");
   }
}
