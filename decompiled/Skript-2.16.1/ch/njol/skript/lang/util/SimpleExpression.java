package ch.njol.skript.lang.util;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.ArrayIterator;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

public abstract class SimpleExpression<T> implements Expression<T>, SyntaxRuntimeErrorProducer {
   private int time = 0;
   private Node node;
   @Nullable
   private ClassInfo<?> returnTypeInfo;

   protected SimpleExpression() {
   }

   @Override
   public boolean preInit() {
      this.node = this.getParser().getNode();
      return Expression.super.preInit();
   }

   @Nullable
   @Override
   public final T getSingle(Event event) {
      T[] values = this.getArray(event);
      if (values.length == 0) {
         return null;
      } else if (values.length > 1) {
         throw new SkriptAPIException("Call to getSingle() on a non-single expression");
      } else {
         return values[0];
      }
   }

   @Override
   public T[] getAll(Event event) {
      T[] values = this.get(event);
      if (values == null) {
         T[] emptyArray = (T[])((Object[])Array.newInstance(this.getReturnType(), 0));

         assert emptyArray != null;

         return emptyArray;
      } else if (values.length == 0) {
         return values;
      } else {
         int numNonNull = 0;

         for (T value : values) {
            if (value != null) {
               numNonNull++;
            }
         }

         if (numNonNull == values.length) {
            return (T[])Arrays.copyOf(values, values.length);
         } else {
            T[] valueArray = (T[])((Object[])Array.newInstance(this.getReturnType(), numNonNull));

            assert valueArray != null;

            int i = 0;

            for (T valuex : values) {
               if (valuex != null) {
                  valueArray[i++] = valuex;
               }
            }

            return valueArray;
         }
      }
   }

   @Override
   public final T[] getArray(Event event) {
      T[] values = this.get(event);
      if (values == null) {
         return (T[])((Object[])Array.newInstance(this.getReturnType(), 0));
      } else if (values.length == 0) {
         return values;
      } else {
         int numNonNull = 0;

         for (T value : values) {
            if (value != null) {
               numNonNull++;
            }
         }

         if (!this.getAnd()) {
            if (values.length == 1 && values[0] != null) {
               return (T[])Arrays.copyOf(values, 1);
            }

            int rand = Utils.random(0, numNonNull);
            T[] valueArray = (T[])((Object[])Array.newInstance(this.getReturnType(), 1));

            for (T valuex : values) {
               if (valuex != null) {
                  if (rand == 0) {
                     valueArray[0] = valuex;
                     return valueArray;
                  }

                  rand--;
               }
            }

            assert false;
         }

         if (numNonNull == values.length) {
            return (T[])Arrays.copyOf(values, values.length);
         } else {
            T[] valueArray = (T[])((Object[])Array.newInstance(this.getReturnType(), numNonNull));
            int i = 0;

            for (T valuexx : values) {
               if (valuexx != null) {
                  valueArray[i++] = valuexx;
               }
            }

            return valueArray;
         }
      }
   }

   @Nullable
   protected abstract T[] get(Event var1);

   @Override
   public final boolean check(Event event, Predicate<? super T> checker) {
      return this.check(event, checker, false);
   }

   @Override
   public final boolean check(Event event, Predicate<? super T> checker, boolean negated) {
      return check(this.get(event), checker, negated, this.getAnd());
   }

   public static <T> boolean check(@Nullable T[] values, Predicate<? super T> checker, boolean invert, boolean and) {
      if (values == null) {
         return invert;
      } else {
         boolean hasElement = false;

         for (T value : values) {
            if (value != null) {
               hasElement = true;
               boolean b = checker.test(value);
               if (and && !b) {
                  return invert;
               }

               if (!and && b) {
                  return !invert;
               }
            }
         }

         return !hasElement ? invert : invert ^ and;
      }
   }

   @Nullable
   protected <R> ConvertedExpression<T, ? extends R> getConvertedExpr(Class<R>... to) {
      assert !CollectionUtils.containsSuperclass(to, this.getReturnType());

      return ConvertedExpression.newInstance(this, to);
   }

   @Nullable
   @Override
   public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
      return (Expression<? extends R>)(CollectionUtils.containsSuperclass(to, this.getReturnType()) ? this : this.getConvertedExpr(to));
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      ClassInfo<?> returnTypeInfo = this.returnTypeInfo;
      if (returnTypeInfo == null) {
         this.returnTypeInfo = returnTypeInfo = Classes.getSuperClassInfo(this.getReturnType());
      }

      Changer<?> changer = returnTypeInfo.getChanger();
      return changer == null ? null : changer.acceptChange(mode);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      ClassInfo<?> returnTypeInfo = this.returnTypeInfo;
      if (returnTypeInfo == null) {
         throw new UnsupportedOperationException();
      } else {
         Changer<?> changer = returnTypeInfo.getChanger();
         if (changer == null) {
            throw new UnsupportedOperationException();
         } else {
            changer.change(this.getArray(event), delta, mode);
         }
      }
   }

   @Override
   public boolean setTime(int time) {
      if (this.getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
         Skript.error("Can't use time states after the event has already passed.");
         return false;
      } else {
         this.time = time;
         return false;
      }
   }

   protected final boolean setTime(int time, Class<? extends Event> applicableEvent) {
      if (this.getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
         Skript.error("Can't use time states after the event has already passed.");
         return false;
      } else if (!this.getParser().isCurrentEvent(applicableEvent)) {
         return false;
      } else {
         this.time = time;
         return true;
      }
   }

   @SafeVarargs
   protected final boolean setTime(int time, Class<? extends Event>... applicableEvents) {
      if (this.getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
         Skript.error("Can't use time states after the event has already passed.");
         return false;
      } else if (!this.getParser().isCurrentEvent(applicableEvents)) {
         return false;
      } else {
         this.time = time;
         return true;
      }
   }

   protected final boolean setTime(int time, Class<? extends Event> applicableEvent, @NotNull Expression<?>... mustbeDefaultVars) {
      if (this.getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
         Skript.error("Can't use time states after the event has already passed.");
         return false;
      } else if (!this.getParser().isCurrentEvent(applicableEvent)) {
         return false;
      } else {
         for (Expression<?> var : mustbeDefaultVars) {
            if (!var.isDefault()) {
               return false;
            }
         }

         this.time = time;
         return true;
      }
   }

   @SafeVarargs
   protected final boolean setTime(int time, Expression<?> mustbeDefaultVar, Class<? extends Event>... applicableEvents) {
      if (this.getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
         Skript.error("Can't use time states after the event has already passed.");
         return false;
      } else if (mustbeDefaultVar == null) {
         Skript.exception(
            new SkriptAPIException(
               "Default expression was null. If the default expression can be null, don't be using 'SimpleExpression#setTime(int, Expression<?>, Class<? extends Event>...)' instead use the setTime without an expression if null."
            )
         );
         return false;
      } else if (!mustbeDefaultVar.isDefault()) {
         return false;
      } else if (this.getParser().isCurrentEvent(applicableEvents)) {
         this.time = time;
         return true;
      } else {
         return false;
      }
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
      return false;
   }

   @Nullable
   @Override
   public Iterator<? extends T> iterator(Event event) {
      return new ArrayIterator(this.getArray(event));
   }

   @Override
   public String toString() {
      return this.toString(null, false);
   }

   @Override
   public Expression<?> getSource() {
      return this;
   }

   @Override
   public Node getNode() {
      return this.node;
   }

   @Override
   public Expression<? extends T> simplify() {
      return this;
   }

   @Override
   public boolean getAnd() {
      return true;
   }

   @Override
   public boolean supportsLoopPeeking() {
      return true;
   }
}
