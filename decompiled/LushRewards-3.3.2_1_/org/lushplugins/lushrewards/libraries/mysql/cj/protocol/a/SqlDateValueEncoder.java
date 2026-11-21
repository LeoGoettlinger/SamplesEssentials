package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;
import org.lushplugins.lushrewards.libraries.mysql.cj.BindValue;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.MysqlType;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyKey;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.WrongArgumentException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalDate;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalTimestamp;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Message;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.TimeUtil;

public class SqlDateValueEncoder extends AbstractValueEncoder {
   @Override
   public String getString(BindValue binding) {
      switch (binding.getMysqlType()) {
         case NULL:
            return "null";
         case DATE:
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            return binding.getCalendar() != null
               ? TimeUtil.getSimpleDateFormat("''yyyy-MM-dd''", binding.getCalendar()).format((Date)binding.getValue())
               : TimeUtil.getSimpleDateFormat(null, "''yyyy-MM-dd''", this.serverSession.getDefaultTimeZone()).format((Date)binding.getValue());
         case DATETIME:
         case TIMESTAMP:
            Timestamp ts = new Timestamp(((java.util.Date)binding.getValue()).getTime());
            return binding.getCalendar() != null
               ? TimeUtil.getSimpleDateFormat("''yyyy-MM-dd HH:mm:ss''", binding.getCalendar()).format(ts)
               : TimeUtil.getSimpleDateFormat(
                     null,
                     "''yyyy-MM-dd HH:mm:ss''",
                     binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                        ? this.serverSession.getSessionTimeZone()
                        : this.serverSession.getDefaultTimeZone()
                  )
                  .format(ts);
         case YEAR:
            Calendar cal = Calendar.getInstance();
            cal.setTime((java.util.Date)binding.getValue());
            return String.valueOf(cal.get(1));
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
      Calendar calendar = binding.getCalendar();
      switch (binding.getMysqlType()) {
         case DATE:
            if (calendar == null) {
               calendar = Calendar.getInstance(this.serverSession.getDefaultTimeZone(), Locale.US);
            }

            calendar.setTime((java.util.Date)binding.getValue());
            calendar.set(11, 0);
            calendar.set(12, 0);
            calendar.set(13, 0);
            this.writeDate(msg, InternalDate.from(calendar));
            return;
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            String val = binding.getCalendar() != null
               ? TimeUtil.getSimpleDateFormat("yyyy-MM-dd", binding.getCalendar()).format((Date)binding.getValue())
               : TimeUtil.getSimpleDateFormat(null, "yyyy-MM-dd", this.serverSession.getDefaultTimeZone()).format((Date)binding.getValue());
            intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(val, this.charEncoding.getValue()));
            return;
         case DATETIME:
         case TIMESTAMP:
            if (calendar == null) {
               calendar = Calendar.getInstance(
                  binding.getMysqlType() == MysqlType.TIMESTAMP && this.propertySet.getBooleanProperty(PropertyKey.preserveInstants).getValue()
                     ? this.serverSession.getSessionTimeZone()
                     : this.serverSession.getDefaultTimeZone(),
                  Locale.US
               );
            }

            Timestamp ts = new Timestamp(((java.util.Date)binding.getValue()).getTime());
            calendar.setTime(ts);
            this.writeDateTime(msg, InternalTimestamp.from(calendar, ts.getNanos()));
            return;
         case YEAR:
            Calendar cal = Calendar.getInstance();
            cal.setTime((java.util.Date)binding.getValue());
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, cal.get(1));
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
      Calendar calendar = Calendar.getInstance(this.serverSession.getDefaultTimeZone(), Locale.US);
      calendar.setTime((java.util.Date)binding.getValue());
      calendar.set(11, 0);
      calendar.set(12, 0);
      calendar.set(13, 0);
      this.writeDate(msg, InternalDate.from(calendar));
   }
}
