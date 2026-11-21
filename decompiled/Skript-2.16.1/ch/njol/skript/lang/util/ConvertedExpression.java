package ch.njol.skript.lang.util;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

public class ConvertedExpression<F, T> implements Expression<T> {
   protected Expression<? extends F> source;
   protected Class<T> to;
   protected Class<T>[] toExact;
   final Converter<? super F, ? extends T> converter;
   private final Collection<ConverterInfo<? super F, ? extends T>> converterInfos;
   private final Class<? extends T>[] returnTypes;
   @Nullable
   private ClassInfo<? super T> returnTypeInfo;

   public ConvertedExpression(Expression<? extends F> source, Class<T> to, ConverterInfo<? super F, ? extends T> info) {
      this.source = source;
      this.to = to;
      this.toExact = new Class[]{to};
      this.converter = info.getConverter();
      this.converterInfos = Collections.singleton(info);
      this.returnTypes = new Class[]{info.getTo()};
   }

   public ConvertedExpression(Expression<? extends F> source, Class<T> to, Collection<ConverterInfo<? super F, ? extends T>> infos, boolean performFromCheck) {
      this(source, new Class[]{to}, infos, performFromCheck);
   }

   public ConvertedExpression(
      Expression<? extends F> source, Class<T>[] toExact, Collection<ConverterInfo<? super F, ? extends T>> infos, boolean performFromCheck
   ) {
      this.source = source;
      this.to = (Class<T>)Utils.getSuperType(toExact);
      this.toExact = toExact;
      this.converterInfos = infos;
      this.returnTypes = this.converterInfos.stream().map(ConverterInfo::getTo).distinct().toArray(Class[]::new);
      this.converter = fromObject -> {
         for (ConverterInfo<? super F, ? extends T> info : this.converterInfos) {
            if (!performFromCheck || info.getFrom().isInstance(fromObject)) {
               T converted = (T)info.getConverter().convert(fromObject);
               if (converted != null) {
                  return converted;
               }
            }
         }

         return null;
      };
   }

   @SafeVarargs
   @Nullable
   public static <F, T> ConvertedExpression<F, T> newInstance(Expression<F> from, Class<T>... to) {
      assert !CollectionUtils.containsSuperclass(to, from.getReturnType());

      List<ConverterInfo<? extends F, ? extends T>> infos = new ArrayList<>();

      for (Class<? extends F> type : from.possibleReturnTypes()) {
         if (CollectionUtils.containsSuperclass(to, type)) {
            infos.add(new ConverterInfo<>(type, type, type::cast, 0));
         } else {
            for (Class<T> toType : to) {
               ConverterInfo<? extends F, T> converter = Converters.getConverterInfo(type, toType);
               if (converter != null) {
                  infos.add(converter);
               }
            }
         }
      }

      if (!infos.isEmpty()) {
         Class<?>[] converterTypes = infos.stream().map(ConverterInfo::getTo).distinct().toArray(Class[]::new);
         return (ConvertedExpression<F, T>)(from instanceof KeyProviderExpression<?> keyProvider
            ? new ConvertedKeyProviderExpression<>(
               (KeyProviderExpression<? extends F>)keyProvider, (Class<T>[])converterTypes, (Collection<ConverterInfo<? super F, ? extends T>>)infos, true
            )
            : new ConvertedExpression<>(from, (Class<T>[])converterTypes, (Collection<ConverterInfo<? super F, ? extends T>>)infos, true));
      } else {
         return null;
      }
   }

   @Override
   public final boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult matcher) {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return debug && event == null
         ? "("
            + this.source.toString(event, debug)
            + " >> "
            + this.converter
            + ": "
            + this.converterInfos.stream().map(Object::toString).collect(Collectors.joining(", "))
            + ")"
         : this.source.toString(event, debug);
   }

   @Override
   public String toString() {
      return this.toString(null, false);
   }

   @Override
   public Class<T> getReturnType() {
      return this.to;
   }

   @Override
   public Class<? extends T>[] possibleReturnTypes() {
      return this.toExact;
   }

   @Override
   public boolean isSingle() {
      return this.source.isSingle();
   }

   @Nullable
   @Override
   public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
      return (Expression<? extends R>)(CollectionUtils.containsSuperclass(to, this.to) ? this : this.source.getConvertedExpression(to));
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      Class<?>[] validClasses = this.source.acceptChange(mode);
      if (validClasses == null) {
         ClassInfo<? super T> returnTypeInfo;
         this.returnTypeInfo = returnTypeInfo = Classes.getSuperClassInfo(this.getReturnType());
         Changer<?> changer = returnTypeInfo.getChanger();
         return changer == null ? null : changer.acceptChange(mode);
      } else {
         return validClasses;
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      ClassInfo<? super T> returnTypeInfo = this.returnTypeInfo;
      if (returnTypeInfo != null) {
         Changer<? super T> changer = returnTypeInfo.getChanger();
         if (changer != null) {
            changer.change(this.getArray(event), delta, mode);
         }
      } else {
         this.source.change(event, delta, mode);
      }
   }

   @Nullable
   @Override
   public T getSingle(Event event) {
      F value = (F)this.source.getSingle(event);
      return (T)(value == null ? null : this.converter.convert(value));
   }

   @Override
   public T[] getArray(Event event) {
      return Converters.convert(this.source.getArray(event), this.to, this.converter);
   }

   @Override
   public T[] getAll(Event event) {
      return Converters.convert(this.source.getAll(event), this.to, this.converter);
   }

   @Override
   public boolean check(Event event, Predicate<? super T> checker, boolean negated) {
      return negated ^ this.check(event, checker);
   }

   @Override
   public boolean check(Event event, Predicate<? super T> checker) {
      return this.source.check(event, value -> {
         T convertedValue = (T)this.converter.convert(value);
         return convertedValue == null ? false : checker.test(convertedValue);
      });
   }

   @Override
   public boolean getAnd() {
      return this.source.getAnd();
   }

   @Override
   public boolean setTime(int time) {
      return this.source.setTime(time);
   }

   @Override
   public int getTime() {
      return this.source.getTime();
   }

   @Override
   public boolean returnNestedStructures(boolean nested) {
      return this.source.returnNestedStructures(nested);
   }

   @Override
   public boolean returnsNestedStructures() {
      return this.source.returnsNestedStructures();
   }

   @Override
   public boolean isDefault() {
      return this.source.isDefault();
   }

   @Override
   public boolean isLoopOf(String input) {
      return false;
   }

   @Nullable
   @Override
   public Iterator<T> iterator(Event event) {
      final Iterator<? extends F> iterator = this.source.iterator(event);
      return iterator == null ? null : new Iterator<T>() {
         @Nullable
         T next = (T)null;

         @Override
         public boolean hasNext() {
            if (this.next != null) {
               return true;
            } else {
               while (this.next == null && iterator.hasNext()) {
                  F value = (F)iterator.next();
                  this.next = (T)(value == null ? null : ConvertedExpression.this.converter.convert(value));
               }

               return this.next != null;
            }
         }

         @Override
         public T next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               T n = this.next;
               this.next = null;

               assert n != null;

               return n;
            }
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }
      };
   }

   @Override
   public Expression<? extends F> getSource() {
      return this.source;
   }

   @Override
   public Expression<? extends T> simplify() {
      Expression<? extends T> convertedExpression = this.source.simplify().getConvertedExpression(this.toExact);
      return (Expression<? extends T>)(convertedExpression != null ? convertedExpression : this);
   }

   @Nullable
   @Override
   public Object[] beforeChange(Expression<?> changed, @Nullable Object[] delta) {
      return this.source.beforeChange(changed, delta);
   }
}
