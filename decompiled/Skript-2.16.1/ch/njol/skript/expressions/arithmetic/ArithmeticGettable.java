package ch.njol.skript.expressions.arithmetic;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public interface ArithmeticGettable<T> {
   @Nullable
   T get(Event var1);

   Class<? extends T> getReturnType();
}
