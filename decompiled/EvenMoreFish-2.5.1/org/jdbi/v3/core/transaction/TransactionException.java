package org.jdbi.v3.core.transaction;

import org.jdbi.v3.core.JdbiException;

public class TransactionException extends JdbiException {
   private static final long serialVersionUID = 1L;

   public TransactionException(String string, Throwable throwable) {
      super(string, throwable);
   }

   public TransactionException(Throwable cause) {
      super(cause);
   }

   public TransactionException(String msg) {
      super(msg);
   }
}
