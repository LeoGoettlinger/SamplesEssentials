package org.lushplugins.lushrewards.libraries.mysql.cj.protocol;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.CJOperationNotSupportedException;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;

public interface ProtocolEntityReader<T extends ProtocolEntity, M extends Message> {
   default T read(ProtocolEntityFactory<T, M> sf) throws IOException {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not allowed");
   }

   default T read(int maxRows, boolean streamResults, M resultPacket, ColumnDefinition metadata, ProtocolEntityFactory<T, M> protocolEntityFactory) throws IOException {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not allowed");
   }
}
