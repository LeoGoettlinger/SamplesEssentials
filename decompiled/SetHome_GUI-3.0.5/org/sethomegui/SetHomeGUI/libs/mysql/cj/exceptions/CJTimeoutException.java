package org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;

public class CJTimeoutException extends CJException {
   private static final long serialVersionUID = -7440108828056331100L;

   public CJTimeoutException() {
      super(Messages.getString("MySQLTimeoutException.0"));
   }

   public CJTimeoutException(String message) {
      super(message);
   }

   public CJTimeoutException(Throwable cause) {
      super(cause);
   }

   public CJTimeoutException(String message, Throwable cause) {
      super(message, cause);
   }
}
