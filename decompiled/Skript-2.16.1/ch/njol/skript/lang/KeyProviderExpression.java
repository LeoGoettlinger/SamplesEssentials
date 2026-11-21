package ch.njol.skript.lang;

import ch.njol.util.coll.iterator.ArrayIterator;
import java.util.Iterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public interface KeyProviderExpression<T> extends Expression<T>, KeyedIterableExpression<T> {
   @NotNull
   String[] getArrayKeys(Event var1) throws IllegalStateException;

   @NotNull
   default String[] getAllKeys(Event event) {
      return this.getArrayKeys(event);
   }

   @Override
   default Iterator<KeyedValue<T>> keyedIterator(Event event) {
      return new ArrayIterator<KeyedValue<T>>(KeyedValue.zip(this.getArray(event), this.getArrayKeys(event)));
   }

   @Override
   default boolean isSingle() {
      return false;
   }

   default boolean canReturnKeys() {
      return true;
   }

   @Override
   default boolean canIterateWithKeys() {
      return this.canReturnKeys();
   }

   default boolean areKeysRecommended() {
      return true;
   }

   static boolean canReturnKeys(Expression<?> expression) {
      return expression instanceof KeyProviderExpression<?> provider && provider.canReturnKeys();
   }

   static boolean areKeysRecommended(Expression<?> expression) {
      return canReturnKeys(expression) && ((KeyProviderExpression)expression).areKeysRecommended();
   }
}
