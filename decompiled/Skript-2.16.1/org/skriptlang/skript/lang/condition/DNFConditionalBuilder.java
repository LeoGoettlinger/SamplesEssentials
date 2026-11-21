package org.skriptlang.skript.lang.condition;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Contract;

public class DNFConditionalBuilder<T> {
   private CompoundConditional<T> root;

   DNFConditionalBuilder() {
      this.root = null;
   }

   DNFConditionalBuilder(Conditional<T> root) {
      if (root instanceof CompoundConditional<T> compoundConditional) {
         this.root = compoundConditional;
      } else {
         this.root = new CompoundConditional<>(Conditional.Operator.OR, root);
      }
   }

   public Conditional<T> build() {
      return (Conditional<T>)Preconditions.checkNotNull(this.root, "Cannot build an empty conditional!");
   }

   @SafeVarargs
   @Contract("_ -> this")
   public final DNFConditionalBuilder<T> and(Conditional<T>... andConditionals) {
      if (this.root == null) {
         this.root = new CompoundConditional<>(Conditional.Operator.AND, andConditionals);
         return this;
      } else {
         List<Conditional<T>> newConditionals = unroll(List.of(andConditionals), Conditional.Operator.AND);
         if (this.root.getOperator() == Conditional.Operator.AND) {
            this.root.addConditionals(newConditionals);
            return this;
         } else {
            List<Conditional<T>> transformedConditionals = new ArrayList<>();
            newConditionals.add(0, null);

            for (Conditional<T> conditional : this.root.getConditionals()) {
               newConditionals.set(0, conditional);
               transformedConditionals.add(new CompoundConditional<>(Conditional.Operator.AND, newConditionals));
            }

            this.root = new CompoundConditional<>(Conditional.Operator.OR, transformedConditionals);
            return this;
         }
      }
   }

   @SafeVarargs
   @Contract("_ -> this")
   public final DNFConditionalBuilder<T> or(Conditional<T>... orConditionals) {
      if (this.root == null) {
         this.root = new CompoundConditional<>(Conditional.Operator.OR, orConditionals);
         return this;
      } else {
         List<Conditional<T>> newConditionals = unroll(List.of(orConditionals), Conditional.Operator.OR);
         if (this.root.getOperator() == Conditional.Operator.OR) {
            this.root.addConditionals(newConditionals);
            return this;
         } else {
            newConditionals.add(0, this.root);
            this.root = new CompoundConditional<>(Conditional.Operator.OR, newConditionals);
            return this;
         }
      }
   }

   @Contract("_ -> this")
   public DNFConditionalBuilder<T> andNot(Conditional<T> conditional) {
      return this.and(Conditional.negate(conditional));
   }

   @Contract("_ -> this")
   public DNFConditionalBuilder<T> orNot(Conditional<T> conditional) {
      return this.or(Conditional.negate(conditional));
   }

   @SafeVarargs
   @Contract("_,_ -> this")
   public final DNFConditionalBuilder<T> add(boolean or, Conditional<T>... conditionals) {
      return or ? this.or(conditionals) : this.and(conditionals);
   }

   @Contract("_,_ -> new")
   private static <T> List<Conditional<T>> unroll(Collection<Conditional<T>> conditionals, Conditional.Operator operator) {
      List<Conditional<T>> newConditionals = new ArrayList<>();

      for (Conditional<T> conditional : conditionals) {
         if (conditional instanceof CompoundConditional<T> compound && compound.getOperator() == operator) {
            newConditionals.addAll(unroll(compound.getConditionals(), operator));
         } else {
            newConditionals.add(conditional);
         }
      }

      return newConditionals;
   }
}
