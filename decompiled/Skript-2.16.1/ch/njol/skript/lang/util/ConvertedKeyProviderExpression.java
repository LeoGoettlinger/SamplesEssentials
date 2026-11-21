package ch.njol.skript.lang.util;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyReceiverExpression;
import ch.njol.skript.lang.KeyedValue;
import com.google.common.collect.Iterators;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

public class ConvertedKeyProviderExpression<F, T> extends ConvertedExpression<F, T> implements KeyProviderExpression<T>, KeyReceiverExpression<T> {
   private final WeakHashMap<Event, String[]> arrayKeysCache = new WeakHashMap<>();
   private final WeakHashMap<Event, String[]> allKeysCache = new WeakHashMap<>();
   private final boolean supportsKeyedChange;

   public ConvertedKeyProviderExpression(KeyProviderExpression<? extends F> source, Class<T> to, ConverterInfo<? super F, ? extends T> info) {
      super(source, to, info);
      this.supportsKeyedChange = source instanceof KeyReceiverExpression;
   }

   public ConvertedKeyProviderExpression(
      KeyProviderExpression<? extends F> source, Class<T>[] toExact, Collection<ConverterInfo<? super F, ? extends T>> converterInfos, boolean performFromCheck
   ) {
      super(source, toExact, converterInfos, performFromCheck);
      this.supportsKeyedChange = source instanceof KeyReceiverExpression;
   }

   @Override
   public T[] getArray(Event event) {
      return !this.canReturnKeys()
         ? super.getArray(event)
         : this.get((F[])this.getSource().getArray(event), this.getSource().getArrayKeys(event), keys -> this.arrayKeysCache.put(event, keys));
   }

   @Override
   public T[] getAll(Event event) {
      return !this.canReturnKeys()
         ? super.getAll(event)
         : this.get((F[])this.getSource().getAll(event), this.getSource().getAllKeys(event), keys -> this.allKeysCache.put(event, keys));
   }

   private T[] get(F[] source, String[] keys, Consumer<String[]> convertedKeysConsumer) {
      T[] converted = (T[])((Object[])Array.newInstance(this.to, source.length));
      Converters.convert(source, converted, this.converter);

      for (int i = 0; i < converted.length; i++) {
         keys[i] = converted[i] != null ? keys[i] : null;
      }

      convertedKeysConsumer.accept((String[])ArrayUtils.removeAllOccurrences(keys, null));
      return (T[])ArrayUtils.removeAllOccurrences(converted, null);
   }

   public KeyProviderExpression<? extends F> getSource() {
      return (KeyProviderExpression<? extends F>)super.getSource();
   }

   @NotNull
   @Override
   public String[] getArrayKeys(Event event) throws IllegalStateException {
      if (!this.arrayKeysCache.containsKey(event)) {
         throw new IllegalStateException();
      } else {
         return this.arrayKeysCache.remove(event);
      }
   }

   @NotNull
   @Override
   public String[] getAllKeys(Event event) {
      if (!this.allKeysCache.containsKey(event)) {
         throw new IllegalStateException();
      } else {
         return this.allKeysCache.remove(event);
      }
   }

   @Override
   public boolean canReturnKeys() {
      return this.getSource().canReturnKeys();
   }

   @Override
   public boolean areKeysRecommended() {
      return this.getSource().areKeysRecommended();
   }

   @Override
   public void change(Event event, @NotNull Object[] delta, Changer.ChangeMode mode, @NotNull String[] keys) {
      if (this.supportsKeyedChange) {
         ((KeyReceiverExpression)this.getSource()).change(event, delta, mode, keys);
      } else {
         this.getSource().change(event, delta, mode);
      }
   }

   @Override
   public boolean isIndexLoop(String input) {
      return this.getSource().isIndexLoop(input);
   }

   @Override
   public boolean isLoopOf(String input) {
      return KeyProviderExpression.super.isLoopOf(input);
   }

   @Override
   public Iterator<KeyedValue<T>> keyedIterator(Event event) {
      Iterator<? extends KeyedValue<? extends F>> source = this.getSource().keyedIterator(event);
      return Iterators.filter(Iterators.transform(source, keyedValue -> {
         assert keyedValue != null;

         T convertedValue = (T)this.converter.convert((F)keyedValue.value());
         return convertedValue != null ? keyedValue.withValue(convertedValue) : null;
      }), Objects::nonNull);
   }
}
