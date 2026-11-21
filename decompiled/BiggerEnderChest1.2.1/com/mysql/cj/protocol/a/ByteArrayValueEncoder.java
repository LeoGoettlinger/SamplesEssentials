package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.util.StringUtils;
import java.nio.charset.StandardCharsets;

public class ByteArrayValueEncoder extends AbstractValueEncoder {
   protected RuntimeProperty<Integer> maxByteArrayAsHex;

   @Override
   public void init(PropertySet pset, ServerSession serverSess, ExceptionInterceptor excInterceptor) {
      super.init(pset, serverSess, excInterceptor);
      this.maxByteArrayAsHex = pset.getIntegerProperty(PropertyKey.maxByteArrayAsHex);
   }

   @Override
   public byte[] getBytes(BindValue binding) {
      return binding.escapeBytesIfNeeded() ? this.escapeBytesIfNeeded((byte[])binding.getValue()) : (byte[])binding.getValue();
   }

   @Override
   public String getString(BindValue binding) {
      return binding.escapeBytesIfNeeded() && binding.getBinaryLength() <= this.maxByteArrayAsHex.getValue().intValue()
         ? StringUtils.toString(this.escapeBytesIfNeeded((byte[])binding.getValue()), StandardCharsets.US_ASCII)
         : "** BYTE ARRAY DATA **";
   }

   @Override
   public void encodeAsBinary(Message msg, BindValue binding) {
      ((NativePacketPayload)msg).writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, (byte[])binding.getValue());
   }
}
