package org.skriptlang.skript.lang.util;

public interface Cyclical<Value extends Number> {
   Value getMaximum();

   Value getMinimum();
}
