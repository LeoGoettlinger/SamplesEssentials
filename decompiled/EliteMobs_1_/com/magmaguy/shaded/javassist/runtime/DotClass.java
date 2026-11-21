package com.magmaguy.shaded.javassist.runtime;

public class DotClass {
   public static NoClassDefFoundError fail(ClassNotFoundException e) {
      return new NoClassDefFoundError(e.getMessage());
   }
}
