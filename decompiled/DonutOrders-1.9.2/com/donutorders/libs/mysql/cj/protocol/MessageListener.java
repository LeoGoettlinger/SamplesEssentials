package com.donutorders.libs.mysql.cj.protocol;

import com.donutorders.libs.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.donutorders.libs.mysql.cj.exceptions.ExceptionFactory;

public interface MessageListener<M extends Message> {
   default boolean processMessage(M message) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not allowed");
   }

   void error(Throwable var1);
}
