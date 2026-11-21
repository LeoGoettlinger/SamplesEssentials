package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExpressionList<T> implements Expression<T> {
   protected final Expression<? extends T>[] expressions;
   private final Class<T> returnType;
   private final Class<?>[] possibleReturnTypes;
   protected boolean and;
   private final boolean single;
   @Nullable
   private final ExpressionList<?> source;
   private int time = 0;

   public ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, boolean and) {
      this(expressions, returnType, and, null);
   }

   public ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, Class<?>[] possibleReturnTypes, boolean and) {
      this(expressions, returnType, possibleReturnTypes, and, null);
   }

   protected ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, boolean and, @Nullable ExpressionList<?> source) {
      this(expressions, returnType, new Class[]{returnType}, and, source);
   }

   protected ExpressionList(
      Expression<? extends T>[] expressions, Class<T> returnType, Class<?>[] possibleReturnTypes, boolean and, @Nullable ExpressionList<?> source
   ) {
      assert expressions != null;

      this.expressions = expressions;
      this.returnType = returnType;
      this.possibleReturnTypes = (Class<?>[])ImmutableSet.copyOf(possibleReturnTypes).toArray(new Class[0]);
      this.and = and;
      if (and) {
         this.single = false;
      } else {
         boolean single = true;

         for (Expression<?> e : expressions) {
            if (!e.isSingle()) {
               single = false;
               break;
            }
         }

         this.single = single;
      }

      this.source = source;
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   @Override
   public T getSingle(Event event) {
      if (!this.single) {
         throw new UnsupportedOperationException();
      } else {
         Expression<? extends T> expression = CollectionUtils.getRandom(this.expressions);
         return (T)(expression != null ? expression.getSingle(event) : null);
      }
   }

   @Override
   public T[] getArray(Event event) {
      if (this.and) {
         return this.getAll(event);
      } else {
         Expression<? extends T> expression = CollectionUtils.getRandom(this.expressions);
         return (T[])(expression != null ? expression.getArray(event) : (Object[])Array.newInstance(this.returnType, 0));
      }
   }

   @Override
   public T[] getAll(Event event) {
      List<T> values = new ArrayList<>();

      for (Expression<? extends T> expr : this.expressions) {
         values.addAll(Arrays.asList(expr.getAll(event)));
      }

      return (T[])values.toArray((T[])((Object[])Array.newInstance(this.returnType, values.size())));
   }

   @Nullable
   @Override
   public Iterator<? extends T> iterator(final Event event) {
      if (!this.and) {
         Expression<? extends T> expression = CollectionUtils.getRandom(this.expressions);
         return expression != null ? expression.iterator(event) : null;
      } else {
         return new Iterator<T>() {
            private int i = 0;
            @Nullable
            private Iterator<? extends T> current = null;

            @Override
            public boolean hasNext() {
               Iterator<? extends T> iterator = this.current;

               while (this.i < ExpressionList.this.expressions.length && (iterator == null || !iterator.hasNext())) {
                  this.current = iterator = ExpressionList.this.expressions[this.i++].iterator(event);
               }

               return iterator != null && iterator.hasNext();
            }

            @Override
            public T next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  Iterator<? extends T> iterator = this.current;
                  if (iterator == null) {
                     throw new NoSuchElementException();
                  } else {
                     T value = (T)iterator.next();

                     assert value != null : this.current;

                     return value;
                  }
               }
            }

            @Override
            public void remove() {
               throw new UnsupportedOperationException();
            }
         };
      }
   }

   @Override
   public boolean isSingle() {
      return this.single;
   }

   @Override
   public boolean check(Event event, Predicate<? super T> checker, boolean negated) {
      return CollectionUtils.check(this.expressions, expr -> expr.check(event, checker) ^ negated, this.and);
   }

   @Override
   public boolean check(Event event, Predicate<? super T> checker) {
      return this.check(event, checker, false);
   }

   @Nullable
   @Override
   public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
      Expression<? extends R>[] exprs = new Expression[this.expressions.length];
      Set<Class<?>> possibleReturnTypeSet = new HashSet<>();

      for (int i = 0; i < exprs.length; i++) {
         if ((exprs[i] = this.expressions[i].getConvertedExpression(to)) == null) {
            return null;
         }

         possibleReturnTypeSet.addAll(Arrays.asList(exprs[i].possibleReturnTypes()));
      }

      Class<?>[] possibleReturnTypes = possibleReturnTypeSet.toArray(new Class[0]);
      return new ExpressionList<>(exprs, (Class<R>)Classes.getSuperClassInfo(possibleReturnTypes).getC(), possibleReturnTypes, this.and, this);
   }

   @Override
   public Class<T> getReturnType() {
      return this.returnType;
   }

   @Override
   public Class<? extends T>[] possibleReturnTypes() {
      return (Class<? extends T>[])this.possibleReturnTypes;
   }

   @Override
   public boolean getAnd() {
      return this.and;
   }

   public void invertAnd() {
      this.and = !this.and;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      List<Class<?>[]> expressionTypes = new ArrayList<>();

      for (Expression<?> expr : this.expressions) {
         Class<?>[] exprTypes = expr.acceptChange(mode);
         if (exprTypes == null) {
            return null;
         }

         expressionTypes.add(exprTypes);
      }

      if (expressionTypes.size() == 1) {
         return expressionTypes.get(0);
      } else {
         Set<Class<?>> acceptable = new LinkedHashSet<>(Arrays.asList(expressionTypes.get(0)));

         for (int i = 1; i < expressionTypes.size(); i++) {
            Set<Class<?>> newAcceptable = new LinkedHashSet<>();

            for (Class<?> candidate : acceptable) {
               for (Class<?> accepted : expressionTypes.get(i)) {
                  if (accepted.isAssignableFrom(candidate)) {
                     newAcceptable.add(candidate);
                     break;
                  }

                  if (candidate.isAssignableFrom(accepted)) {
                     newAcceptable.add(accepted);
                     break;
                  }
               }
            }

            acceptable = newAcceptable;
            if (newAcceptable.isEmpty()) {
               return new Class[0];
            }
         }

         return acceptable.toArray(new Class[0]);
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
      if (this.and) {
         for (Expression<?> expr : this.expressions) {
            expr.change(event, delta, mode);
         }
      } else {
         int i = ThreadLocalRandom.current().nextInt(this.expressions.length);
         this.expressions[i].change(event, delta, mode);
      }
   }

   @Override
   public <R> void changeInPlace(Event event, Function<T, R> changeFunction, boolean getAll) {
      if (!this.and && !getAll) {
         int i = ThreadLocalRandom.current().nextInt(this.expressions.length);
         this.expressions[i].changeInPlace(event, changeFunction, false);
      } else {
         for (Expression<?> expr : this.expressions) {
            expr.changeInPlace(event, changeFunction, getAll);
         }
      }
   }

   @Override
   public boolean setTime(int time) {
      boolean ok = false;

      for (Expression<?> e : this.expressions) {
         ok |= e.setTime(time);
      }

      if (ok) {
         this.time = time;
      }

      return ok;
   }

   @Override
   public int getTime() {
      return this.time;
   }

   @Override
   public boolean isDefault() {
      return false;
   }

   @Override
   public boolean isLoopOf(String input) {
      for (Expression<?> expression : this.expressions) {
         if (expression.isLoopOf(input)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public Expression<?> getSource() {
      ExpressionList<?> source = this.source;
      return source == null ? this : source;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      StringBuilder result = new StringBuilder("(");

      for (int i = 0; i < this.expressions.length; i++) {
         if (i != 0) {
            if (i == this.expressions.length - 1) {
               result.append(this.and ? " and " : " or ");
            } else {
               result.append(", ");
            }
         }

         result.append(this.expressions[i].toString(event, debug));
      }

      result.append(")");
      if (debug) {
         result.append("[").append(this.returnType).append("]");
      }

      return result.toString();
   }

   @Override
   public String toString() {
      return this.toString(null, false);
   }

   public Expression<? extends T>[] getExpressions() {
      return this.expressions;
   }

   public List<Expression<? extends T>> getAllExpressions() {
      List<Expression<? extends T>> expressions = new ArrayList<>();

      for (Expression<? extends T> expression : this.expressions) {
         if (expression instanceof ExpressionList<? extends T> innerList) {
            expressions.addAll(innerList.getAllExpressions());
         } else {
            expressions.add(expression);
         }
      }

      return expressions;
   }

   @Override
   public Expression<T> simplify() {
      boolean isLiteralList = true;

      for (int i = 0; i < this.expressions.length; i++) {
         this.expressions[i] = this.expressions[i].simplify();
         isLiteralList &= this.expressions[i] instanceof Literal;
      }

      if (isLiteralList) {
         Literal<? extends T>[] ls = Arrays.copyOf(this.expressions, this.expressions.length, Literal[].class);
         return new LiteralList<>(ls, this.returnType, this.and);
      } else {
         return this;
      }
   }
}
