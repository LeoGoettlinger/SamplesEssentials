package org.skriptlang.skript.log.runtime;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.SyntaxElement;
import org.jetbrains.annotations.NotNull;

public interface SyntaxRuntimeErrorProducer extends RuntimeErrorProducer {
   Node getNode();

   @NotNull
   @Override
   default ErrorSource getErrorSource() {
      return ErrorSource.fromNodeAndElement(this.getNode(), (SyntaxElement)this);
   }
}
