package ch.njol.skript.lang.simplification;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.lang.util.SimpleLiteral;
import java.util.function.Function;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class SimplifiedLiteral<T> extends SimpleLiteral<T> {
   public static <T> SimplifiedLiteral<T> fromExpression(Expression<T> original) {
      if (original instanceof SimplifiedLiteral<T> literal) {
         return literal;
      } else {
         Event event = ContextlessEvent.get();
         T[] values = original.getAll(event);
         return new SimplifiedLiteral<>(values, (Class<T>)values.getClass().getComponentType(), original.getAnd(), original);
      }
   }

   public SimplifiedLiteral(T[] data, Class<T> type, boolean and, Expression<T> source) {
      super(data, type, and, source);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return this.source.acceptChange(mode);
   }

   @Nullable
   @Override
   public Object[] beforeChange(Expression<?> changed, @Nullable Object[] delta) {
      return this.source.beforeChange(changed, delta);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
      this.source.change(event, delta, mode);
   }

   @Override
   public boolean isLoopOf(String input) {
      return this.source.isLoopOf(input);
   }

   @Override
   public <R> void changeInPlace(Event event, Function<T, R> changeFunction) {
      this.getSource().changeInPlace(event, changeFunction);
   }

   @Override
   public <R> void changeInPlace(Event event, Function<T, R> changeFunction, boolean getAll) {
      this.getSource().changeInPlace(event, changeFunction, getAll);
   }

   @Override
   public Expression<T> getSource() {
      return (Expression<T>)this.source;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return debug ? "[" + this.source.toString(event, true) + " (SIMPLIFIED)]" : this.source.toString(event, false);
   }
}
