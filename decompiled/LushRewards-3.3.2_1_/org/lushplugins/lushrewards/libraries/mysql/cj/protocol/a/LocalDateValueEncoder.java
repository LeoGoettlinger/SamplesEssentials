package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.lushplugins.lushrewards.libraries.mysql.cj.BindValue;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.WrongArgumentException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalDate;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalTimestamp;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Message;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.TimeUtil;

public class LocalDateValueEncoder extends AbstractValueEncoder {
   @Override
   public String getString(BindValue binding) {
      switch (binding.getMysqlType()) {
         case NULL:
            return "null";
         case DATE: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(((LocalDate)binding.getValue()).format(TimeUtil.DATE_FORMATTER));
            sb.append("'");
            return sb.toString();
         }
         case DATETIME:
         case TIMESTAMP: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(LocalDateTime.of((LocalDate)binding.getValue(), TimeUtil.DEFAULT_TIME).format(TimeUtil.DATETIME_FORMATTER_WITH_OPTIONAL_MICROS));
            sb.append("'");
            return sb.toString();
         }
         case YEAR:
            return String.valueOf(((LocalDate)binding.getValue()).getYear());
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(binding.getValue().toString());
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
         case DATE:
            this.writeDate(msg, InternalDate.from((LocalDate)binding.getValue()));
            return;
         case DATETIME:
         case TIMESTAMP:
            this.writeDateTime(msg, InternalTimestamp.from(LocalDateTime.of((LocalDate)binding.getValue(), TimeUtil.DEFAULT_TIME)));
            return;
         case YEAR:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, ((LocalDate)binding.getValue()).getYear());
            return;
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            intoPacket.writeBytes(
               NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(binding.getValue().toString(), this.charEncoding.getValue())
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
      this.writeDate(msg, InternalDate.from((LocalDate)binding.getValue()));
   }
}
