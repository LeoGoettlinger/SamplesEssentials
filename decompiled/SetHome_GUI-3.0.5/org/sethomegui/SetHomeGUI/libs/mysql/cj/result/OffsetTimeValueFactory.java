package org.sethomegui.SetHomeGUI.libs.mysql.cj.result;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.WarningListener;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.DataConversionException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.DataReadException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalDate;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTime;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTimestamp;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.MysqlTextValueDecoder;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.StringUtils;

public class OffsetTimeValueFactory extends AbstractDateTimeValueFactory<OffsetTime> {
   private WarningListener warningListener;
   private TimeZone tz;

   public OffsetTimeValueFactory(PropertySet pset, TimeZone tz) {
      super(pset);
      this.tz = tz;
   }

   public OffsetTimeValueFactory(PropertySet pset, TimeZone tz, WarningListener warningListener) {
      this(pset, tz);
      this.warningListener = warningListener;
   }

   OffsetTime localCreateFromDate(InternalDate idate) {
      return LocalTime.of(0, 0).atOffset(ZoneOffset.ofTotalSeconds(this.tz.getRawOffset() / 1000));
   }

   public OffsetTime localCreateFromTime(InternalTime it) {
      if (it.getHours() >= 0 && it.getHours() < 24) {
         return LocalTime.of(it.getHours(), it.getMinutes(), it.getSeconds(), it.getNanos()).atOffset(ZoneOffset.ofTotalSeconds(this.tz.getRawOffset() / 1000));
      } else {
         throw new DataReadException(Messages.getString("ResultSet.InvalidTimeValue", new Object[]{it.toString()}));
      }
   }

   public OffsetTime localCreateFromTimestamp(InternalTimestamp its) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{this.getTargetTypeName()}));
      }

      return this.createFromTime(new InternalTime(its.getHours(), its.getMinutes(), its.getSeconds(), its.getNanos(), its.getScale()));
   }

   public OffsetTime localCreateFromDatetime(InternalTimestamp its) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{this.getTargetTypeName()}));
      }

      return this.createFromTime(new InternalTime(its.getHours(), its.getMinutes(), its.getSeconds(), its.getNanos(), its.getScale()));
   }

   public OffsetTime createFromBytes(byte[] bytes, int offset, int length, Field f) {
      if (length == 0 && this.pset.getBooleanProperty(PropertyKey.emptyStringsConvertToZero).getValue()) {
         return this.createFromLong(0L);
      } else {
         String s = StringUtils.toString(bytes, offset, length, f.getEncoding());
         byte[] newBytes = s.getBytes();
         if (MysqlTextValueDecoder.isDate(s)) {
            return this.createFromDate(MysqlTextValueDecoder.getDate(newBytes, 0, newBytes.length));
         } else if (MysqlTextValueDecoder.isTime(s)) {
            return this.createFromTime(MysqlTextValueDecoder.getTime(newBytes, 0, newBytes.length, f.getDecimals()));
         } else if (MysqlTextValueDecoder.isTimestamp(s)) {
            return this.createFromTimestamp(MysqlTextValueDecoder.getTimestamp(newBytes, 0, newBytes.length, f.getDecimals()));
         } else {
            try {
               return OffsetTime.parse(s);
            } catch (DateTimeParseException var8) {
               throw new DataConversionException(Messages.getString("ResultSet.UnableToConvertString", new Object[]{s, this.getTargetTypeName()}));
            }
         }
      }
   }

   @Override
   public String getTargetTypeName() {
      return OffsetTime.class.getName();
   }
}
