package org.skriptlang.skript.util;

public interface Executable<Caller, Result> {
   Result execute(Caller var1, Object... var2);
}
