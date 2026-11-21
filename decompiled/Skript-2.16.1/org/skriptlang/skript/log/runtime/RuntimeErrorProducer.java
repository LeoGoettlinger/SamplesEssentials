package org.skriptlang.skript.log.runtime;

import ch.njol.skript.Skript;
import java.util.logging.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface RuntimeErrorProducer {
   @Contract(" -> new")
   @NotNull
   ErrorSource getErrorSource();

   default void error(String message) {
      this.getRuntimeErrorManager().error(new RuntimeError(Level.SEVERE, this.getErrorSource(), message, null));
   }

   default void error(String message, String highlight) {
      this.getRuntimeErrorManager().error(new RuntimeError(Level.SEVERE, this.getErrorSource(), message, highlight));
   }

   default void warning(String message) {
      this.getRuntimeErrorManager().error(new RuntimeError(Level.WARNING, this.getErrorSource(), message, null));
   }

   default void warning(String message, String highlight) {
      this.getRuntimeErrorManager().error(new RuntimeError(Level.WARNING, this.getErrorSource(), message, highlight));
   }

   default RuntimeErrorManager getRuntimeErrorManager() {
      return Skript.getRuntimeErrorManager();
   }
}
