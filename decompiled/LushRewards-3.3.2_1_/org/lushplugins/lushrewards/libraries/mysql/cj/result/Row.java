package org.lushplugins.lushrewards.libraries.mysql.cj.result;

import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.CJOperationNotSupportedException;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ColumnDefinition;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntity;

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
