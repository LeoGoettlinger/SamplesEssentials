package org.skriptlang.skript.log.runtime;

import java.util.logging.Level;
import org.jetbrains.annotations.Nullable;

public interface RuntimeErrorConsumer {
   void printError(RuntimeError var1);

   @Nullable
   default RuntimeErrorFilter getFilter() {
      return RuntimeErrorManager.standardFilter;
   }

   void printFrameOutput(Frame.FrameOutput var1, Level var2);
}
