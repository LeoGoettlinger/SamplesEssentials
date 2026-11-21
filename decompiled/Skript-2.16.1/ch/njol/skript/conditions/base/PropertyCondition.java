package ch.njol.skript.conditions.base;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.util.function.Predicate;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

public abstract class PropertyCondition<T> extends Condition implements Predicate<T> {
   public static final Priority DEFAULT_PRIORITY = Priority.before(SyntaxInfo.PATTERN_MATCHES_EVERYTHING);
   private Expression<? extends T> expr;

   public static <E extends Condition> SyntaxInfo.Builder<? extends SyntaxInfo.Builder<?, E>, E> infoBuilder(
      Class<E> condition, PropertyCondition.PropertyType propertyType, String property, String type
   ) {
      if (type.contains("%")) {
         throw new SkriptAPIException("The type argument must not contain any '%'s");
      } else {
         return (SyntaxInfo.Builder<? extends SyntaxInfo.Builder<?, E>, E>)SyntaxInfo.builder(condition)
            .priority(DEFAULT_PRIORITY)
            .addPatterns(getPatterns(propertyType, property, type));
      }
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static void register(Class<? extends Condition> condition, String property, String type) {
      register(condition, PropertyCondition.PropertyType.BE, property, type);
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static void register(Class<? extends Condition> condition, PropertyCondition.PropertyType propertyType, String property, String type) {
      Skript.registerCondition(condition, Condition.ConditionType.PROPERTY, getPatterns(propertyType, property, type));
   }

   public static String[] getPatterns(PropertyCondition.PropertyType propertyType, String property, String type) {
      if (type.contains("%")) {
         throw new SkriptAPIException("The type argument must not contain any '%'s");
      } else {
         return switch (propertyType) {
            case BE -> new String[]{"%" + type + "% (is|are) " + property, "%" + type + "% (isn't|is not|aren't|are not) " + property};
            case CAN -> new String[]{"%" + type + "% can " + property, "%" + type + "% (can't|cannot|can not) " + property};
            case HAVE -> new String[]{"%" + type + "% (has|have) " + property, "%" + type + "% (doesn't|does not|do not|don't) have " + property};
            case WILL -> new String[]{"%" + type + "% will " + property, "%" + type + "% (will (not|neither)|won't) " + property};
         };
      }
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.expr = (Expression<? extends T>)expressions[0];
      this.setNegated(matchedPattern == 1);
      return true;
   }

   @Override
   public final boolean check(Event event) {
      return this.expr.check(event, this, this.isNegated());
   }

   public abstract boolean check(T var1);

   @Override
   public final boolean test(T value) {
      return this.check(value);
   }

   protected abstract String getPropertyName();

   protected PropertyCondition.PropertyType getPropertyType() {
      return PropertyCondition.PropertyType.BE;
   }

   public final Expression<? extends T> getExpr() {
      return this.expr;
   }

   protected final void setExpr(Expression<? extends T> expr) {
      this.expr = expr;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return toString(this, this.getPropertyType(), event, debug, this.expr, this.getPropertyName());
   }

   public static String toString(
      Condition condition, PropertyCondition.PropertyType propertyType, @Nullable Event event, boolean debug, Expression<?> expr, String property
   ) {
      switch (propertyType) {
         case BE:
            return expr.toString(event, debug) + (expr.isSingle() ? " is " : " are ") + (condition.isNegated() ? "not " : "") + property;
         case CAN:
            return expr.toString(event, debug) + (condition.isNegated() ? " can't " : " can ") + property;
         case HAVE:
            if (expr.isSingle()) {
               return expr.toString(event, debug) + (condition.isNegated() ? " doesn't have " : " has ") + property;
            }

            return expr.toString(event, debug) + (condition.isNegated() ? " don't have " : " have ") + property;
         case WILL:
            return expr.toString(event, debug) + (condition.isNegated() ? " won't " : " will ") + "be " + property;
         default:
            assert false;

            return null;
      }
   }

   @NotNull
   @Override
   public Predicate<T> and(@NotNull Predicate<? super T> other) {
      throw new UnsupportedOperationException("Combining property conditions is undefined behaviour");
   }

   @NotNull
   @Override
   public Predicate<T> negate() {
      throw new UnsupportedOperationException("Negating property conditions without setNegated is undefined behaviour");
   }

   @NotNull
   @Override
   public Predicate<T> or(@NotNull Predicate<? super T> other) {
      throw new UnsupportedOperationException("Combining property conditions is undefined behaviour");
   }

   public static enum PropertyType {
      BE,
      CAN,
      HAVE,
      WILL;
   }
}
