package org.skriptlang.skript.lang.condition;

import ch.njol.util.Kleenean;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

class CompoundConditional<T> implements Conditional<T> {
   private final LinkedHashSet<Conditional<T>> componentConditionals = new LinkedHashSet<>();
   private final Conditional.Operator operator;
   private boolean useCache;

   CompoundConditional(Conditional.Operator operator, @NotNull Collection<Conditional<T>> conditionals) {
      Preconditions.checkArgument(!conditionals.isEmpty(), "CompoundConditionals must contain at least 1 component conditional.");
      Preconditions.checkArgument(
         operator != Conditional.Operator.NOT || conditionals.size() == 1, "The NOT operator cannot be applied to multiple Conditionals."
      );
      this.componentConditionals.addAll(conditionals);
      this.useCache = conditionals.stream().anyMatch(cond -> cond instanceof CompoundConditional);
      this.operator = operator;
   }

   @SafeVarargs
   CompoundConditional(Conditional.Operator operator, Conditional<T>... conditionals) {
      this(operator, List.of(conditionals));
   }

   @Override
   public Kleenean evaluate(T context) {
      Map<Conditional<T>, Kleenean> cache = null;
      if (this.useCache) {
         cache = new HashMap<>();
      }

      return this.evaluate(context, cache);
   }

   @Override
   public Kleenean evaluate(T context, Map<Conditional<T>, Kleenean> cache) {
      return switch (this.operator) {
         case OR -> {
            Kleenean result = Kleenean.FALSE;

            for (Conditional<T> conditional : this.componentConditionals) {
               result = conditional.evaluateOr(result, context, cache);
            }

            yield result;
         }
         case AND -> {
            Kleenean result = Kleenean.TRUE;

            for (Conditional<T> conditional : this.componentConditionals) {
               result = conditional.evaluateAnd(result, context, cache);
            }

            yield result;
         }
         case NOT -> {
            if (this.componentConditionals.size() > 1) {
               throw new IllegalStateException("Cannot apply NOT to multiple conditionals! Cannot evaluate.");
            }

            Iterator var4 = this.componentConditionals.iterator();
            if (!var4.hasNext()) {
               throw new IllegalStateException("Cannot apply NOT to zero conditionals! Cannot evaluate.");
            }

            Conditional<T> conditional = (Conditional<T>)var4.next();
            yield conditional.evaluate(context, cache).not();
         }
      };
   }

   @Unmodifiable
   public List<Conditional<T>> getConditionals() {
      return this.componentConditionals.stream().toList();
   }

   public Conditional.Operator getOperator() {
      return this.operator;
   }

   @SafeVarargs
   public final void addConditionals(Conditional<T>... conditionals) {
      this.addConditionals(List.of(conditionals));
   }

   public void addConditionals(Collection<Conditional<T>> conditionals) {
      this.componentConditionals.addAll(conditionals);
      this.useCache = this.useCache | conditionals.stream().anyMatch(cond -> cond instanceof CompoundConditional);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      String output = this.joinConditionals(event, debug);
      if (this.componentConditionals.size() > 1) {
         output = "(" + output + ")";
      }

      return this.operator == Conditional.Operator.NOT ? "!" + output : output;
   }

   private String joinConditionals(@Nullable Event event, boolean debug) {
      return this.componentConditionals
         .stream()
         .map(conditional -> conditional.toString(event, debug))
         .collect(Collectors.joining(" " + this.operator.getSymbol() + " "));
   }

   @Override
   public String toString() {
      return this.toString(null, false);
   }
}
