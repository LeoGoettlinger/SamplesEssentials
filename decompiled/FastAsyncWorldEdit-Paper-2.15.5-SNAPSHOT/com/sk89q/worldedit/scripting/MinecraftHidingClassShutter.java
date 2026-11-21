package com.sk89q.worldedit.scripting;

import org.mozilla.javascript.ClassShutter;

class MinecraftHidingClassShutter implements ClassShutter {
   public boolean visibleToScripts(String fullClassName) {
      return fullClassName.contains(".") || fullClassName.length() >= 4;
   }
}
