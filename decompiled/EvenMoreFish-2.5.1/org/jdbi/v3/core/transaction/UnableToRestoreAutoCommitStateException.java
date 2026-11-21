package org.jdbi.v3.core.transaction;

import org.jdbi.v3.core.JdbiException;

public class UnableToRestoreAutoCommitStateException extends JdbiException {
   private static final long serialVersionUID = 2433069110223543423L;

   public UnableToRestoreAutoCommitStateException(Throwable throwable) {
      super(throwable);
   }
}
