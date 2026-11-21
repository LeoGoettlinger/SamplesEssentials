package ch.njol.skript.lang.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Container;
import ch.njol.util.Kleenean;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ContainerExpression extends SimpleExpression<Object> {
   final Expression<? extends Container<?>> expr;
   private final Class<?> type;

   public ContainerExpression(Expression<? extends Container<?>> expr, Class<?> type) {
      this.expr = expr;
      this.type = type;
   }

   @Override
   protected Object[] get(Event e) {
      throw new UnsupportedOperationException("ContainerExpression must only be used by Loops");
   }

   @Nullable
   @Override
   public Iterator<Object> iterator(Event event) {
      final Iterator<? extends Container<?>> iterator = this.expr.iterator(event);
      return iterator == null ? null : new Iterator<Object>() {
         @Nullable
         private Iterator<?> current;

         @Override
         public boolean hasNext() {
            Iterator<?> current = this.current;

            while (iterator.hasNext() && (current == null || !current.hasNext())) {
               this.current = current = iterator.next().containerIterator();
            }

            return current != null && current.hasNext();
         }

         @Override
         public Object next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               Iterator<?> current = this.current;
               if (current == null) {
                  throw new NoSuchElementException();
               } else {
                  Object value = current.next();

                  assert value != null : this.current + "; " + ContainerExpression.this.expr;

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

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<?> getReturnType() {
      return this.type;
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.expr.toString(event, debug);
   }
}
