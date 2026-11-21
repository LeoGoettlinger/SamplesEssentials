package dev.cwhead.GravesX.module.util;

import dev.cwhead.GravesX.module.ModuleContext;
import java.util.Objects;

public interface LibraryImporter {
   void importLibrary(ModuleContext var1, String... var2);

   default void importLibrary(ModuleContext ctx, ModuleInfo.LibraryDef def) {
      Objects.requireNonNull(ctx, "ctx");
      Objects.requireNonNull(def, "def");
      String coords = def.coordinates();
      if (coords != null && !coords.isBlank()) {
         this.importLibrary(ctx, coords);
      }
   }
}
