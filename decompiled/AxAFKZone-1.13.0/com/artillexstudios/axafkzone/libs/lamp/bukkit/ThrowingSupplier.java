package com.artillexstudios.axafkzone.libs.lamp.bukkit;

@FunctionalInterface
public interface ThrowingSupplier<T> {
   T get() throws Exception;
}
