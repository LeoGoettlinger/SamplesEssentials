package org.skriptlang.skript.lang.condition;

import ch.njol.skript.lang.Debuggable;
import ch.njol.util.Kleenean;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Conditional<T> extends Debuggable {
   @Contract(
      pure = true
   )
   Kleenean evaluate(T var1);

   @Contract(
      pure = true
   )
   default Kleenean evaluate(T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
      return cache == null ? this.evaluate(context) : cache.computeIfAbsent(this, cond -> cond.evaluate(context));
   }

   @Contract(
      pure = true
   )
   default Kleenean evaluateAnd(Conditional<T> other, T context) {
      return this.evaluateAnd(other.evaluate(context, null), context, null);
   }

   @Contract(
      pure = true
   )
   default Kleenean evaluateAnd(Conditional<T> other, T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
      return this.evaluateAnd(other.evaluate(context, cache), context, cache);
   }

   @Contract(
      pure = true
   )
   default Kleenean evaluateAnd(Kleenean other, T context) {
      return this.evaluateAnd(other, context, null);
   }

   @Contract(
      pure = true
   )
   default Kleenean evaluateAnd(Kleenean other, T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
      return other.isFalse() ? other : other.and(this.evaluate(context, cache));
   }

   @Contract(
      pure = true
   )
   default Kleenean evaluateOr(Conditional<T> other, T context) {
      return this.evaluateOr(other.evaluate(context, null), context, null);
   }

   @Contract(
      pure = true
   )
   default Kleenean evaluateOr(Conditional<T> other, T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
      return this.evaluateOr(other.evaluate(context, cache), context, cache);
   }

   @Contract(
      pure = true
   )
   default Kleenean evaluateOr(Kleenean other, T context) {
      return this.evaluateOr(other, context, null);
   }

   @Contract(
      pure = true
   )
   default Kleenean evaluateOr(Kleenean other, T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
      return other.isTrue() ? other : other.or(this.evaluate(context, cache));
   }

   @Contract(
      pure = true
   )
   default Kleenean evaluateNot(T context) {
      return this.evaluateNot(context, null);
   }

   @Contract(
      pure = true
   )
   default Kleenean evaluateNot(T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
      return this.evaluate(context, cache).not();
   }

   @Contract("_, _ -> new")
   static <T> Conditional<T> compound(Conditional.Operator operator, Collection<Conditional<T>> conditionals) {
      Preconditions.checkArgument(operator != Conditional.Operator.NOT, "Cannot combine conditionals using NOT!");
      return new CompoundConditional<>(operator, conditionals);
   }

   @Contract("_, _ -> new")
   static <T> Conditional<T> compound(Conditional.Operator operator, Conditional<T>... conditionals) {
      return compound(operator, List.of(conditionals));
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   @NotNull
   static <T> DNFConditionalBuilder<T> builderDNF(Class<T> ignoredContextClass) {
      return new DNFConditionalBuilder<>();
   }

   @Contract("_ -> new")
   @NotNull
   static <T> DNFConditionalBuilder<T> builderDNF(Conditional<T> conditional) {
      return new DNFConditionalBuilder<>(conditional);
   }

   static <T> Conditional<T> negate(Conditional<T> conditional) {
      if (!(conditional instanceof CompoundConditional<T> compound)) {
         return new CompoundConditional<>(Conditional.Operator.NOT, conditional);
      } else {
         return (Conditional<T>)(switch (compound.getOperator()) {
            case AND -> {
               List<Conditional<T>> newConditionals = new ArrayList<>();

               for (Conditional<T> cond : compound.getConditionals()) {
                  newConditionals.add(negate(cond));
               }

               yield new CompoundConditional<>(Conditional.Operator.OR, newConditionals);
            }
            case OR -> {
               List<Conditional<T>> newConditionals = new ArrayList<>();

               for (Conditional<T> cond : compound.getConditionals()) {
                  newConditionals.add(negate(cond));
               }

               yield new CompoundConditional<>(Conditional.Operator.AND, newConditionals);
            }
            case NOT -> (Conditional)compound.getConditionals().getFirst();
         });
      }
   }

   public static enum Operator {
      AND("&&"),
      OR("||"),
      NOT("!");

      private final String symbol;

      private Operator(String symbol) {
         this.symbol = symbol;
      }

      String getSymbol() {
         return this.symbol;
      }
   }
}
