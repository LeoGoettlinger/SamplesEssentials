package org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;

public class OperationCancelledException extends CJException {
   private static final long serialVersionUID = 9001418688349454695L;

   public OperationCancelledException() {
      super(Messages.getString("MySQLStatementCancelledException.0"));
   }

   public OperationCancelledException(String message) {
      super(message);
   }

   public OperationCancelledException(Throwable cause) {
      super(cause);
   }

   public OperationCancelledException(String message, Throwable cause) {
      super(message, cause);
   }
}
