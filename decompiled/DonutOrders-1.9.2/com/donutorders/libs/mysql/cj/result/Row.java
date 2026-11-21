package com.donutorders.libs.mysql.cj.result;

import com.donutorders.libs.mysql.cj.Messages;
import com.donutorders.libs.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.donutorders.libs.mysql.cj.exceptions.ExceptionFactory;
import com.donutorders.libs.mysql.cj.protocol.ColumnDefinition;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntity;

public interface Row extends ProtocolEntity {
   <T> T getValue(int var1, ValueFactory<T> var2);

   default Row setMetadata(ColumnDefinition columnDefinition) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(
         CJOperationNotSupportedException.class, Messages.getString("OperationNotSupportedException.0")
      );
   }

   default byte[] getBytes(int columnIndex) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(
         CJOperationNotSupportedException.class, Messages.getString("OperationNotSupportedException.0")
      );
   }

   default void setBytes(int columnIndex, byte[] value) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(
         CJOperationNotSupportedException.class, Messages.getString("OperationNotSupportedException.0")
      );
   }

   boolean getNull(int var1);

   boolean wasNull();
}
