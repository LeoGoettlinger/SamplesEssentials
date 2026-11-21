package com.artillexstudios.axrankmenu.libs.axapi.utils.functions;

@FunctionalInterface
public interface ThrowingSupplier<T> {
   T get() throws Exception;
}
