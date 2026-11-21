package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a;

import java.sql.Blob;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.BindValue;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Message;

public class BlobValueEncoder extends InputStreamValueEncoder {
   @Override
   public byte[] getBytes(BindValue binding) {
      try {
         return this.streamToBytes(((Blob)binding.getValue()).getBinaryStream(), binding.getScaleOrLength(), null);
      } catch (Throwable var3) {
         throw ExceptionFactory.createException(var3.getMessage(), var3, this.exceptionInterceptor);
      }
   }

   @Override
   public void encodeAsText(Message msg, BindValue binding) {
      try {
         this.streamToBytes(((Blob)binding.getValue()).getBinaryStream(), binding.getScaleOrLength(), (NativePacketPayload)msg);
      } catch (Throwable var4) {
         throw ExceptionFactory.createException(var4.getMessage(), var4, this.exceptionInterceptor);
      }
   }
}
