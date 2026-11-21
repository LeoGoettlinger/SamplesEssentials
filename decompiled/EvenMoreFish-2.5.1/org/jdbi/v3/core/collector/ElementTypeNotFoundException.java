package org.jdbi.v3.core.collector;

import org.jdbi.v3.core.JdbiException;

public class ElementTypeNotFoundException extends JdbiException {
   public ElementTypeNotFoundException(String string, Throwable throwable) {
      super(string, throwable);
   }

   public ElementTypeNotFoundException(Throwable cause) {
      super(cause);
   }

   public ElementTypeNotFoundException(String message) {
      super(message);
   }
}
