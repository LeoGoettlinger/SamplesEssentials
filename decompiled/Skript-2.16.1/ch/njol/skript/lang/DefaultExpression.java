package ch.njol.skript.lang;

public interface DefaultExpression<T> extends Expression<T> {
   boolean init();

   @Override
   boolean isDefault();
}
