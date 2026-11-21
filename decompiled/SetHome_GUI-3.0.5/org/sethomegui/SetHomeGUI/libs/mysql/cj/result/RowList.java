package org.sethomegui.SetHomeGUI.libs.mysql.cj.result;

import java.util.Iterator;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJOperationNotSupportedException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;

public interface RowList extends Iterator<Row> {
   int RESULT_SET_SIZE_UNKNOWN = -1;

   default Row previous() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(
         CJOperationNotSupportedException.class, Messages.getString("OperationNotSupportedException.0")
      );
   }

   default Row get(int n) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(
         CJOperationNotSupportedException.class, Messages.getString("OperationNotSupportedException.0")
      );
   }

   default int getPosition() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(
         CJOperationNotSupportedException.class, Messages.getString("OperationNotSupportedException.0")
      );
   }

   default int size() {
      return -1;
   }
}
