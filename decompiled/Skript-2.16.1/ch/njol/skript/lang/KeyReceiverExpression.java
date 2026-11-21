package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public interface KeyReceiverExpression<T> extends Expression<T> {
   default boolean acceptsNestedStructures() {
      return false;
   }

   void change(Event var1, @NotNull Object[] var2, Changer.ChangeMode var3, @NotNull String[] var4);
}
