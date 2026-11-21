package com.viaversion.viaversion.libs.mcstructs.converter.codec;

@FunctionalInterface
public interface ThrowingFunction<T, R> {
   R apply(T var1) throws Throwable;
}
