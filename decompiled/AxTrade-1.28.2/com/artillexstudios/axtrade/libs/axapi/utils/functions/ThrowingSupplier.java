package com.artillexstudios.axtrade.libs.axapi.utils.functions;

@FunctionalInterface
public interface ThrowingSupplier<T> {
   T get() throws Exception;
}
