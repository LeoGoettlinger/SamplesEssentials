package com.github.jikoo.openinv.lib.planarwrappers.function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
   R apply(T var1) throws E;
}
