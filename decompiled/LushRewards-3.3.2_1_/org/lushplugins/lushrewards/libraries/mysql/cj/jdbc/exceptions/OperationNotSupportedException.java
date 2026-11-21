package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.exceptions;

import java.sql.SQLException;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;

public class OperationNotSupportedException extends SQLException {
   static final long serialVersionUID = 474918612056813430L;

   public OperationNotSupportedException() {
      super(Messages.getString("RowDataDynamic.3"), "S1009");
   }

   public OperationNotSupportedException(String message) {
      super(message, "S1009");
   }
}
