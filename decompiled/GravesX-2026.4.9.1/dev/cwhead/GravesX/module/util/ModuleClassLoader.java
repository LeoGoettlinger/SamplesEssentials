package dev.cwhead.GravesX.module.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

public final class ModuleClassLoader extends URLClassLoader {
   public ModuleClassLoader(URL jarUrl, ClassLoader parent) {
      super(new URL[]{Objects.requireNonNull(jarUrl, "jarUrl")}, Objects.requireNonNull(parent, "parent"));
   }

   @Override
   public void close() {
      try {
         super.close();
      } catch (Exception var2) {
      }
   }
}
