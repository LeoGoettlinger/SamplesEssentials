package ch.njol.skript.lang;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.bukkit.event.Event;

public interface KeyedIterableExpression<T> extends Expression<T> {
   boolean canIterateWithKeys();

   Iterator<KeyedValue<T>> keyedIterator(Event var1);

   default Stream<KeyedValue<T>> keyedStream(Event event) {
      return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.keyedIterator(event), 0), false);
   }

   @Override
   default boolean isLoopOf(String input) {
      return this.canIterateWithKeys() && this.isIndexLoop(input);
   }

   default boolean isIndexLoop(String input) {
      return input.equalsIgnoreCase("index");
   }

   static boolean canIterateWithKeys(Expression<?> expression) {
      return expression instanceof KeyedIterableExpression<?> keyed && keyed.canIterateWithKeys();
   }
}
