package com.donutorders.libs.mysql.cj.protocol.a;

import com.donutorders.libs.mysql.cj.BindValue;
import com.donutorders.libs.mysql.cj.conf.PropertySet;
import com.donutorders.libs.mysql.cj.exceptions.ExceptionInterceptor;
import com.donutorders.libs.mysql.cj.protocol.Message;
import com.donutorders.libs.mysql.cj.protocol.ServerSession;
import com.donutorders.libs.mysql.cj.util.StringUtils;

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
