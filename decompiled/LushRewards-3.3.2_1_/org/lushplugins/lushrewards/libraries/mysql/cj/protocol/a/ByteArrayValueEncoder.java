package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a;

import java.nio.charset.StandardCharsets;
import org.lushplugins.lushrewards.libraries.mysql.cj.BindValue;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyKey;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.RuntimeProperty;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionInterceptor;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Message;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ServerSession;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;

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
