package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a;

import java.sql.Clob;
import org.lushplugins.lushrewards.libraries.mysql.cj.BindValue;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;

public class ClobValueEncoder extends ReaderValueEncoder {
   @Override
   public byte[] getBytes(BindValue binding) {
      try {
         return this.readBytes(((Clob)binding.getValue()).getCharacterStream(), binding);
      } catch (Throwable var3) {
         throw ExceptionFactory.createException(var3.getMessage(), var3, this.exceptionInterceptor);
      }
   }
}
