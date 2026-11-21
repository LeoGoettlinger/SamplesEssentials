package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.BindValue;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.MysqlType;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.WrongArgumentException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalDate;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTime;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTimestamp;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Message;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.StringUtils;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.TimeUtil;

public class UtilDateValueEncoder extends AbstractValueEncoder {
   private SimpleDateFormat tsdf = null;

   @Override
   public String getString(BindValue binding) {
      if (binding.isNull()) {
         return "null";
      } else {
         Timestamp x = this.adjustTimestamp(new Timestamp(((Date)binding.getValue()).getTime()), binding.getField(), binding.keepOrigNanos());
         switch (binding.getMysqlType()) {
            case DATE:
               return binding.getCalendar() != null
                  ? TimeUtil.getSimpleDateFormat("''yyyy-MM-dd''", binding.getCalendar()).format(new java.sql.Date(((Date)binding.getValue()).getTime()))
                  : TimeUtil.getSimpleDateFormat(null, "''yyyy-MM-dd''", this.serverSession.getDefaultTimeZone())
                     .format(new java.sql.Date(((Date)binding.getValue()).getTime()));
            case DATETIME:
            case TIMESTAMP:
            case CHAR:
            case VARCHAR:
            case TINYTEXT:
            case TEXT:
            case MEDIUMTEXT:
            case LONGTEXT: {
               StringBuilder sb = new StringBuilder();
               if (binding.getCalendar() != null) {
                  sb.append(TimeUtil.getSimpleDateFormat("''yyyy-MM-dd HH:mm:ss", binding.getCalendar()).format(x));
               } else {
                  this.tsdf = TimeUtil.getSimpleDateFormat(
                     this.tsdf,
                     "''yyyy-MM-dd HH:mm:ss",
                     binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                        ? this.serverSession.getSessionTimeZone()
                        : this.serverSession.getDefaultTimeZone()
                  );
                  sb.append(this.tsdf.format(x));
               }

               if (this.serverSession.getCapabilities().serverSupportsFracSecs() && x.getNanos() > 0) {
                  sb.append('.');
                  sb.append(TimeUtil.formatNanos(x.getNanos(), 6));
               }

               sb.append('\'');
               return sb.toString();
            }
            case YEAR:
               Calendar cal = Calendar.getInstance();
               cal.setTime((Date)binding.getValue());
               return String.valueOf(cal.get(1));
            case TIME: {
               StringBuilder sb = new StringBuilder("'");
               sb.append(
                  this.adjustLocalTime(new Timestamp(((Date)binding.getValue()).getTime()).toLocalDateTime().toLocalTime(), binding.getField())
                     .format(TimeUtil.TIME_FORMATTER_WITH_OPTIONAL_MICROS)
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
   }

   @Override
   public void encodeAsBinary(Message msg, BindValue binding) {
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      Timestamp x = this.adjustTimestamp(new Timestamp(((Date)binding.getValue()).getTime()), binding.getField(), binding.keepOrigNanos());
      Calendar calendar = binding.getCalendar();
      switch (binding.getMysqlType()) {
         case DATE:
            if (calendar == null) {
               calendar = Calendar.getInstance(this.serverSession.getDefaultTimeZone(), Locale.US);
            }

            calendar.setTime((Date)binding.getValue());
            calendar.set(11, 0);
            calendar.set(12, 0);
            calendar.set(13, 0);
            this.writeDate(msg, InternalDate.from(calendar));
            return;
         case DATETIME:
         case TIMESTAMP:
            if (calendar == null) {
               calendar = Calendar.getInstance(
                  binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                     ? this.serverSession.getSessionTimeZone()
                     : this.serverSession.getDefaultTimeZone(),
                  Locale.US
               );
            }

            calendar.setTime(x);
            this.writeDateTime(msg, InternalTimestamp.from(calendar, x.getNanos()));
            return;
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            StringBuilder sb = new StringBuilder();
            if (binding.getCalendar() != null) {
               sb.append(TimeUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss", binding.getCalendar()).format(x));
            } else {
               this.tsdf = TimeUtil.getSimpleDateFormat(
                  this.tsdf,
                  "yyyy-MM-dd HH:mm:ss",
                  binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                     ? this.serverSession.getSessionTimeZone()
                     : this.serverSession.getDefaultTimeZone()
               );
               sb.append(this.tsdf.format(x));
            }

            if (this.serverSession.getCapabilities().serverSupportsFracSecs() && x.getNanos() > 0) {
               sb.append('.');
               sb.append(TimeUtil.formatNanos(x.getNanos(), 6));
            }

            intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(sb.toString(), this.charEncoding.getValue()));
            return;
         case YEAR:
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date)binding.getValue());
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, cal.get(1));
            return;
         case TIME:
            Time t = this.adjustTime(new Time(x.getTime()));
            if (calendar == null) {
               calendar = Calendar.getInstance(this.serverSession.getDefaultTimeZone(), Locale.US);
            }

            calendar.setTime(t);
            this.writeTime(
               msg,
               InternalTime.from(
                  calendar, this.adjustTimestamp(new Timestamp(((Date)binding.getValue()).getTime()), binding.getField(), binding.keepOrigNanos()).getNanos()
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
      Date date = (Date)binding.getValue();
      Calendar calendar = Calendar.getInstance(this.serverSession.getDefaultTimeZone(), Locale.US);
      calendar.setTime(date);
      InternalTimestamp internalTimestamp = InternalTimestamp.from(calendar, (int)TimeUnit.MILLISECONDS.toNanos(calendar.get(14)));
      internalTimestamp.setOffset((int)TimeUnit.MILLISECONDS.toMinutes(calendar.getTimeZone().getOffset(date.getTime())));
      this.writeDateTimeWithOffset(msg, internalTimestamp);
   }
}
