package ch.njol.util;

@Deprecated(
   since = "2.10.0",
   forRemoval = true
)
@FunctionalInterface
public interface Checker<T> extends java.util.function.Predicate<T> {
   boolean check(T var1);

   @Override
   default boolean test(T t) {
      return this.check(t);
   }
}
