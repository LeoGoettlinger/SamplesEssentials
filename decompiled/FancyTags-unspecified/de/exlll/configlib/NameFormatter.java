package de.exlll.configlib;

import java.util.function.Function;

@FunctionalInterface
public interface NameFormatter extends Function<String, String> {
   String format(String var1);

   default String apply(String name) {
      return this.format(name);
   }
}
