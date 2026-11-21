package ch.njol.skript.lang.util;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.NonNullIterator;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Predicate;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

public class SimpleLiteral<T> implements Literal<T>, DefaultExpression<T> {
   protected final Class<T> type;
   private final boolean isDefault;
   private final boolean and;
   protected final Expression<?> source;
   protected transient T[] data;
   @Nullable
   private ClassInfo<? super T> returnTypeInfo;

   public SimpleLiteral(T[] data, Class<T> type, boolean and) {
      this(data, type, and, null);
   }

   public SimpleLiteral(T[] data, Class<T> type, boolean and, @Nullable Expression<?> source) {
      this(data, type, and, false, source);
   }

   public SimpleLiteral(T[] data, Class<T> type, boolean and, boolean isDefault, @Nullable Expression<?> source) {
      assert data != null;

      assert type != null;

      this.data = data;
      this.type = type;
      this.and = data.length <= 1 || and;
      this.isDefault = isDefault;
      this.source = (Expression<?>)(source == null ? this : source);
   }

   public SimpleLiteral(T data, boolean isDefault) {
      this(data, isDefault, null);
   }

   public SimpleLiteral(T data, boolean isDefault, @Nullable Expression<?> source) {
      assert data != null;

      this.data = (T[])((Object[])Array.newInstance(data.getClass(), 1));
      this.data[0] = data;
      this.type = (Class<T>)data.getClass();
      this.and = true;
      this.isDefault = isDefault;
      this.source = (Expression<?>)(source == null ? this : source);
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean init() {
      return true;
   }

   private T[] data() {
      return (T[])Arrays.copyOf(this.data, this.data.length);
   }

   @Override
   public T[] getArray() {
      return this.data();
   }

   @Override
   public T[] getArray(Event event) {
      return this.data();
   }

   @Override
   public T[] getAll() {
      return this.data();
   }

   @Override
   public T[] getAll(Event event) {
      return this.data();
   }

   @Override
   public T getSingle() {
      return CollectionUtils.getRandom(this.data);
   }

   @Override
   public T getSingle(Event event) {
      return this.getSingle();
   }

   @Override
   public Class<T> getReturnType() {
      return this.type;
   }

   @Nullable
   @Override
   public <R> Literal<? extends R> getConvertedExpression(Class<R>... to) {
      if (CollectionUtils.containsSuperclass(to, this.type)) {
         return this;
      } else {
         R[] parsedData = Converters.convert(this.data(), to, (Class<R>)Utils.getSuperType(to));
         return parsedData.length != this.data.length ? null : new ConvertedLiteral<>(this, parsedData, (Class<R>)Utils.getSuperType(to));
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return debug ? "[" + Classes.toString(this.data, this.getAnd(), StringMode.DEBUG) + "]" : Classes.toString(this.data, this.getAnd());
   }

   @Override
   public String toString() {
      return this.toString(null, false);
   }

   @Override
   public boolean isSingle() {
      return !this.getAnd() || this.data.length <= 1;
   }

   @Override
   public boolean isDefault() {
      return this.isDefault;
   }

   @Override
   public boolean check(Event event, Predicate<? super T> checker, boolean negated) {
      return SimpleExpression.check(this.data, checker, negated, this.getAnd());
   }

   @Override
   public boolean check(Event event, Predicate<? super T> checker) {
      return SimpleExpression.check(this.data, checker, false, this.getAnd());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      ClassInfo<? super T> returnTypeInfo = this.returnTypeInfo;
      if (returnTypeInfo == null) {
         this.returnTypeInfo = returnTypeInfo = Classes.getSuperClassInfo(this.getReturnType());
      }

      Changer<? super T> changer = returnTypeInfo.getChanger();
      return changer == null ? null : changer.acceptChange(mode);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
      ClassInfo<? super T> returnTypeInfo = this.returnTypeInfo;
      if (returnTypeInfo == null) {
         throw new UnsupportedOperationException();
      } else {
         Changer<? super T> changer = returnTypeInfo.getChanger();
         if (changer == null) {
            throw new UnsupportedOperationException();
         } else {
            changer.change(this.getArray(), delta, mode);
         }
      }
   }

   @Override
   public boolean getAnd() {
      return this.and;
   }

   @Override
   public boolean setTime(int time) {
      return false;
   }

   @Override
   public int getTime() {
      return 0;
   }

   public NonNullIterator<T> iterator(Event event) {
      return new NonNullIterator<T>() {
         private int i = 0;

         @Nullable
         @Override
         protected T getNext() {
            return this.i == SimpleLiteral.this.data.length ? null : SimpleLiteral.this.data[this.i++];
         }
      };
   }

   @Override
   public boolean isLoopOf(String input) {
      return false;
   }

   @Override
   public Expression<?> getSource() {
      return this.source;
   }

   @Override
   public Expression<T> simplify() {
      return this;
   }
}
