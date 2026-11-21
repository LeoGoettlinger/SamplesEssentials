package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a;

import java.time.OffsetTime;
import java.time.ZoneOffset;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.BindValue;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.WrongArgumentException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTime;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Message;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.StringUtils;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.TimeUtil;

public class OffsetTimeValueEncoder extends AbstractValueEncoder {
   @Override
   public String getString(BindValue binding) {
      switch (binding.getMysqlType()) {
         case NULL:
            return "null";
         case TIME: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               this.adjustLocalTime(
                     ((OffsetTime)binding.getValue())
                        .withOffsetSameInstant(ZoneOffset.ofTotalSeconds(this.serverSession.getDefaultTimeZone().getRawOffset() / 1000))
                        .toLocalTime(),
                     binding.getField()
                  )
                  .format(TimeUtil.TIME_FORMATTER_WITH_OPTIONAL_MICROS)
            );
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
            sb.append(
               ((OffsetTime)binding.getValue())
                  .format(
                     this.sendFractionalSeconds.getValue() && ((OffsetTime)binding.getValue()).getNano() > 0
                        ? TimeUtil.TIME_FORMATTER_WITH_NANOS_WITH_OFFSET
                        : TimeUtil.TIME_FORMATTER_NO_FRACT_WITH_OFFSET
                  )
            );
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
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      switch (binding.getMysqlType()) {
         case TIME:
            this.writeTime(
               msg,
               InternalTime.from(
                  this.adjustLocalTime(
                     ((OffsetTime)binding.getValue())
                        .withOffsetSameInstant(ZoneOffset.ofTotalSeconds(this.serverSession.getDefaultTimeZone().getRawOffset() / 1000))
                        .toLocalTime(),
                     binding.getField()
                  )
               )
            );
            return;
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            intoPacket.writeBytes(
               NativeConstants.StringSelfDataType.STRING_LENENC,
               StringUtils.getBytes(
                  ((OffsetTime)binding.getValue())
                     .format(
                        this.sendFractionalSeconds.getValue() && ((OffsetTime)binding.getValue()).getNano() > 0
                           ? TimeUtil.TIME_FORMATTER_WITH_NANOS_WITH_OFFSET
                           : TimeUtil.TIME_FORMATTER_NO_FRACT_WITH_OFFSET
                     ),
                  this.charEncoding.getValue()
               )
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
      this.writeTime(msg, InternalTime.from((OffsetTime)binding.getValue()));
   }
}
