package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.exceptions;

import java.sql.SQLTransactionRollbackException;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.DeadlockTimeoutRollbackMarker;

public class MySQLTransactionRollbackException extends SQLTransactionRollbackException implements DeadlockTimeoutRollbackMarker {
   static final long serialVersionUID = 6034999468737899730L;

   public MySQLTransactionRollbackException(String reason, String SQLState, int vendorCode) {
      super(reason, SQLState, vendorCode);
   }

   public MySQLTransactionRollbackException(String reason, String SQLState) {
      super(reason, SQLState);
   }

   public MySQLTransactionRollbackException(String reason) {
      super(reason);
   }

   public MySQLTransactionRollbackException() {
   }
}
