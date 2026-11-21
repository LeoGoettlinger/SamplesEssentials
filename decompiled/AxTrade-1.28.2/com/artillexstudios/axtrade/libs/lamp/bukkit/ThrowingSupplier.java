package com.artillexstudios.axtrade.libs.lamp.bukkit;

@FunctionalInterface
public interface ThrowingSupplier<T> {
   T get() throws Exception;
}
