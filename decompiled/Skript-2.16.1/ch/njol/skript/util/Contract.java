package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;

public interface Contract {
   boolean isSingle(Expression<?>... var1);

   @Nullable
   Class<?> getReturnType(Expression<?>... var1);
}
