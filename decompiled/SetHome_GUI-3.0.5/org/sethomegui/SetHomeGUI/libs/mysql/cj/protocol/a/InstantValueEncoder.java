package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Locale;
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

public class InstantValueEncoder extends AbstractValueEncoder {
   @Override
   public String getString(BindValue binding) {
      switch (binding.getMysqlType()) {
         case NULL:
            return "null";
         case DATE: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               ((Instant)binding.getValue())
                  .atOffset(ZoneOffset.UTC)
                  .atZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId())
                  .toLocalDate()
                  .format(TimeUtil.DATE_FORMATTER)
            );
            sb.append("'");
            return sb.toString();
         }
         case TIME: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               this.adjustLocalTime(
                     ((Instant)binding.getValue()).atOffset(ZoneOffset.UTC).atZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).toLocalTime(),
                     binding.getField()
                  )
                  .format(TimeUtil.TIME_FORMATTER_WITH_OPTIONAL_MICROS)
            );
            sb.append("'");
            return sb.toString();
         }
         case DATETIME:
         case TIMESTAMP: {
            Timestamp x = this.adjustTimestamp(
               Timestamp.valueOf(
                  ((Instant)binding.getValue())
                     .atOffset(ZoneOffset.UTC)
                     .atZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId())
                     .toLocalDateTime()
               ),
               binding.getField(),
               binding.keepOrigNanos()
            );
            StringBuilder sb = new StringBuilder();
            sb.append(
               TimeUtil.getSimpleDateFormat(
                     null,
                     "''yyyy-MM-dd HH:mm:ss",
                     binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                        ? this.serverSession.getSessionTimeZone()
                        : this.serverSession.getDefaultTimeZone()
                  )
                  .format(x)
            );
            if (this.serverSession.getCapabilities().serverSupportsFracSecs() && x.getNanos() > 0) {
               sb.append('.');
               sb.append(TimeUtil.formatNanos(x.getNanos(), 6));
            }

            sb.append('\'');
            return sb.toString();
         }
         case YEAR:
            return String.valueOf(
               ((Instant)binding.getValue()).atOffset(ZoneOffset.UTC).atZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).getYear()
            );
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               ((Instant)binding.getValue())
                  .atOffset(ZoneOffset.UTC)
                  .format(
                     this.sendFractionalSeconds.getValue() && ((Instant)binding.getValue()).getNano() > 0
                        ? TimeUtil.DATETIME_FORMATTER_WITH_NANOS_WITH_OFFSET
                        : TimeUtil.DATETIME_FORMATTER_NO_FRACT_WITH_OFFSET
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
         case DATE:
            this.writeDate(
               msg,
               InternalDate.from(
                  ((Instant)binding.getValue()).atOffset(ZoneOffset.UTC).atZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).toLocalDate()
               )
            );
            return;
         case TIME:
            this.writeTime(
               msg,
               InternalTime.from(
                  this.adjustLocalTime(
                     ((Instant)binding.getValue()).atOffset(ZoneOffset.UTC).atZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).toLocalTime(),
                     binding.getField()
                  )
               )
            );
            return;
         case DATETIME:
         case TIMESTAMP:
            Timestamp ts = this.adjustTimestamp(
               Timestamp.valueOf(
                  ((Instant)binding.getValue())
                     .atOffset(ZoneOffset.UTC)
                     .atZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId())
                     .toLocalDateTime()
               ),
               binding.getField(),
               binding.keepOrigNanos()
            );
            Calendar calendar = Calendar.getInstance(
               binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                  ? this.serverSession.getSessionTimeZone()
                  : this.serverSession.getDefaultTimeZone(),
               Locale.US
            );
            calendar.setTime(ts);
            this.writeDateTime(msg, InternalTimestamp.from(calendar, ts.getNanos()));
            return;
         case YEAR:
            intoPacket.writeInteger(
               NativeConstants.IntegerDataType.INT4,
               ((Instant)binding.getValue()).atOffset(ZoneOffset.UTC).atZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).getYear()
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
                  ((Instant)binding.getValue())
                     .atOffset(ZoneOffset.UTC)
                     .format(
                        this.sendFractionalSeconds.getValue() && ((Instant)binding.getValue()).getNano() > 0
                           ? TimeUtil.DATETIME_FORMATTER_WITH_NANOS_WITH_OFFSET
                           : TimeUtil.DATETIME_FORMATTER_NO_FRACT_WITH_OFFSET
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
      this.writeDateTimeWithOffset(msg, InternalTimestamp.from(((Instant)binding.getValue()).atOffset(ZoneOffset.UTC)));
   }
}
