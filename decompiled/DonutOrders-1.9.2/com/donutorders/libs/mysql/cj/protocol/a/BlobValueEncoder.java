package com.donutorders.libs.mysql.cj.protocol.a;

import com.donutorders.libs.mysql.cj.BindValue;
import com.donutorders.libs.mysql.cj.exceptions.ExceptionFactory;
import com.donutorders.libs.mysql.cj.protocol.Message;
import java.sql.Blob;

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
