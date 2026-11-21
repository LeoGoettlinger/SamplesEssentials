package ch.njol.skript.expressions.arithmetic;

import ch.njol.skript.lang.Expression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;

public record ArithmeticExpressionInfo<T>(Expression<? extends T> expression) implements ArithmeticGettable<T> {
   @Nullable
   @Override
   public T get(Event event) {
      T object = (T)this.expression.getSingle(event);
      return object == null ? Arithmetics.getDefaultValue(this.expression.getReturnType()) : object;
   }

   @Override
   public Class<? extends T> getReturnType() {
      return this.expression.getReturnType();
   }
}
