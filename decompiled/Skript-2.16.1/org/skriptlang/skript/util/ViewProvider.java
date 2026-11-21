package org.skriptlang.skript.util;

import org.jetbrains.annotations.Contract;

public interface ViewProvider<T> {
   @Contract("-> new")
   T unmodifiableView();
}
