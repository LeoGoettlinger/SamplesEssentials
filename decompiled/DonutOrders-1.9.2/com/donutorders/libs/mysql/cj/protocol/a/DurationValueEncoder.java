package com.donutorders.libs.mysql.cj.protocol.a;

import com.donutorders.libs.mysql.cj.BindValue;
import com.donutorders.libs.mysql.cj.Messages;
import com.donutorders.libs.mysql.cj.exceptions.ExceptionFactory;
import com.donutorders.libs.mysql.cj.exceptions.WrongArgumentException;
import com.donutorders.libs.mysql.cj.protocol.InternalTime;
import com.donutorders.libs.mysql.cj.protocol.Message;
import com.donutorders.libs.mysql.cj.util.StringUtils;
import com.donutorders.libs.mysql.cj.util.TimeUtil;
import java.time.Duration;

public class DurationValueEncoder extends AbstractValueEncoder {
   @Override
   public String getString(BindValue binding) {
      switch (binding.getMysqlType()) {
         case NULL:
            return "null";
         case TIME: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(TimeUtil.getDurationString(this.adjustDuration(Duration.ofNanos(((Duration)binding.getValue()).toNanos()), binding.getField())));
            sb.append("'");
            return sb.toString();
         }
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(TimeUtil.getDurationString((Duration)binding.getValue()));
            sb.append("'");
            return sb.toString();
         }
         default:
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class,
               Messages.getString("PreparedStatement.67", new Object[]{binding.getValue().getClass().getName(), binding.getMysqlType().toString()}),
               this.exceptionInterceptor
            );
      }
   }

   @Override
   public void encodeAsBinary(Message msg, BindValue binding) {
      Duration x = (Duration)binding.getValue();
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      switch (binding.getMysqlType()) {
         case TIME:
            this.writeTime(msg, InternalTime.from(this.adjustDuration(Duration.ofNanos(x.toNanos()), binding.getField())));
            return;
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            intoPacket.writeBytes(
               NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(TimeUtil.getDurationString(x), this.charEncoding.getValue())
            );
            return;
         default:
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class,
               Messages.getString("PreparedStatement.67", new Object[]{binding.getValue().getClass().getName(), binding.getMysqlType().toString()}),
               this.exceptionInterceptor
            );
      }
   }

   @Override
   public void encodeAsQueryAttribute(Message msg, BindValue binding) {
      this.writeTime(msg, InternalTime.from((Duration)binding.getValue()));
   }
}
