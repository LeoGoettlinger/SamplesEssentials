package org.lushplugins.lushrewards.libraries.mysql.cj.protocol;

import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.CJOperationNotSupportedException;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;

public interface MessageListener<M extends Message> {
   default boolean processMessage(M message) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not allowed");
   }

   void error(Throwable var1);
}
