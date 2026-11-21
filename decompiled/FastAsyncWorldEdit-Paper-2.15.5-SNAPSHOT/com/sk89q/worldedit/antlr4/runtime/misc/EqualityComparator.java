package com.sk89q.worldedit.antlr4.runtime.misc;

public interface EqualityComparator<T> {
   int hashCode(T var1);

   boolean equals(T var1, T var2);
}
