package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Amount")
@Description({"The amount or size of something.", "Please note that <code>amount of %items%</code> will not return the number of items, but the number of stacks, e.g. 1 for a stack of 64 torches. To get the amount of items in a stack, see the <a href='#ExprItemAmount'>item amount</a> expression."})
@Example("message \"There are %number of all players% players online!\"")
@Since({"1.0"})
@Deprecated(
   since = "2.13",
   forRemoval = true
)
public class ExprAmount extends SimpleExpression<Number> {
   private ExpressionList<?> exprs;
   @Nullable
   private Expression<AnyAmount> any;
   @Nullable
   private Variable<?> list;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (matchedPattern == 0) {
         this.any = (Expression<AnyAmount>)exprs[0];
         return true;
      } else {
         this.exprs = exprs[0] instanceof ExpressionList<?> exprList ? exprList : new ExpressionList<>(new Expression[]{exprs[0]}, Object.class, false);
         this.exprs = (ExpressionList<?>)LiteralUtils.defendExpression(this.exprs);
         if (!LiteralUtils.canInitSafely(this.exprs)) {
            return false;
         } else if (this.exprs.isSingle()) {
            Skript.error(
               "'"
                  + this.exprs.toString(null, Skript.debug())
                  + "' can only ever have one value at most, thus the 'amount of ...' expression is useless. Use '... exists' instead to find out whether the expression has a value."
            );
            return false;
         } else {
            if (exprs[0] instanceof Variable<?> variable) {
               this.list = variable;
            }

            return true;
         }
      }
   }

   protected Number[] get(Event event) {
      if (this.any != null) {
         return new Number[]{this.any.getOptionalSingle(event).orElse(() -> 0).amount()};
      } else {
         return this.list != null ? new Long[]{(long)this.list.size(event)} : new Long[]{(long)this.exprs.getArray(event).length};
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.any != null) {
         return switch (mode) {
            case SET, ADD, RESET, DELETE, REMOVE -> (Class[])CollectionUtils.array(Number.class);
            default -> null;
         };
      } else {
         return super.acceptChange(mode);
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (this.any == null) {
         super.change(event, delta, mode);
      } else {
         double amount = delta != null ? ((Number)delta[0]).doubleValue() : 1.0;
         switch (mode) {
            case SET:
            case RESET:
            case DELETE:
               for (AnyAmount any : this.any.getArray(event)) {
                  if (any.supportsAmountChange()) {
                     any.setAmount(amount);
                  }
               }
               break;
            case REMOVE:
               amount = -amount;
            case ADD:
               for (AnyAmount obj : this.any.getArray(event)) {
                  if (obj.supportsAmountChange()) {
                     obj.setAmount(obj.amount().doubleValue() + amount);
                  }
               }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return this.any != null ? Number.class : Long.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.any != null ? "amount of " + this.any.toString(event, debug) : "amount of " + this.exprs.toString(event, debug);
   }

   static {
      if (!SkriptConfig.useTypeProperties.value()) {
         Skript.registerExpression(
            ExprAmount.class, Number.class, ExpressionType.PROPERTY, "[the] (amount|number|size) of %numbered%", "[the] (amount|number|size) of %objects%"
         );
      }
   }
}
