package ch.njol.skript.lang;

import org.jetbrains.annotations.Nullable;

public interface Literal<T> extends Expression<T> {
   T[] getArray();

   T getSingle();

   @Nullable
   <R> Literal<? extends R> getConvertedExpression(Class<R>... var1);

   T[] getAll();
}
