package org.skriptlang.skript.util;

import org.jetbrains.annotations.ApiStatus.Internal;

public interface Validated {
   @Internal
   void invalidate() throws UnsupportedOperationException;

   boolean valid();

   static Validated validator() {
      return new Validator();
   }
}
