package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.BindValue;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJOperationNotSupportedException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.WrongArgumentException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Message;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.StringUtils;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.Util;

public class ReaderValueEncoder extends AbstractValueEncoder {
   @Override
   public byte[] getBytes(BindValue binding) {
      return this.readBytes((Reader)binding.getValue(), binding);
   }

   @Override
   public String getString(BindValue binding) {
      return "'** STREAM DATA **'";
   }

   @Override
   public void encodeAsBinary(Message msg, BindValue binding) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   protected byte[] readBytes(Reader reader, BindValue binding) {
      try {
         char[] c = null;
         int len = 0;
         boolean useLength = this.propertySet.getBooleanProperty(PropertyKey.useStreamLengthsInPrepStmts).getValue();
         String clobEncoding = binding.isNational() ? null : this.propertySet.getStringProperty(PropertyKey.clobCharacterEncoding).getStringValue();
         if (clobEncoding == null) {
            clobEncoding = this.charEncoding.getStringValue();
         }

         long scaleOrLength = binding.getScaleOrLength();
         byte[] bytes;
         if (useLength && scaleOrLength != -1L) {
            c = new char[(int)scaleOrLength];
            int numCharsRead = Util.readFully(reader, c, (int)scaleOrLength);
            bytes = StringUtils.getBytes(new String(c, 0, numCharsRead), clobEncoding);
         } else {
            c = new char[4096];
            StringBuilder buf = new StringBuilder();

            while ((len = reader.read(c)) != -1) {
               buf.append(c, 0, len);
            }

            bytes = StringUtils.getBytes(buf.toString(), clobEncoding);
         }

         return this.escapeBytesIfNeeded(bytes);
      } catch (UnsupportedEncodingException var11) {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, var11.toString(), var11, this.exceptionInterceptor);
      } catch (IOException var12) {
         throw ExceptionFactory.createException(var12.toString(), var12, this.exceptionInterceptor);
      }
   }
}
