package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJOperationNotSupportedException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;

public interface MessageListener<M extends Message> {
   default boolean processMessage(M message) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not allowed");
   }

   void error(Throwable var1);
}
