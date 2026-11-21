package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a;

import org.lushplugins.lushrewards.libraries.mysql.cj.BindValue;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionInterceptor;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Message;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ServerSession;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;

public class NullValueEncoder extends AbstractValueEncoder {
   @Override
   public void init(PropertySet pset, ServerSession serverSess, ExceptionInterceptor excInterceptor) {
      super.init(pset, serverSess, excInterceptor);
   }

   @Override
   public byte[] getBytes(BindValue binding) {
      return StringUtils.getBytes("null");
   }

   @Override
   public String getString(BindValue binding) {
      return "NULL";
   }

   @Override
   public void encodeAsBinary(Message msg, BindValue binding) {
   }
}
