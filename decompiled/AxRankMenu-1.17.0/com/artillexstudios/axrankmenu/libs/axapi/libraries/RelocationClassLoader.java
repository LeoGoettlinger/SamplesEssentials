package com.artillexstudios.axrankmenu.libs.axapi.libraries;

import java.net.URL;
import java.net.URLClassLoader;

public class RelocationClassLoader extends URLClassLoader {
   public RelocationClassLoader(URL[] urls) {
      super(urls);
   }

   static {
      ClassLoader.registerAsParallelCapable();
   }
}
